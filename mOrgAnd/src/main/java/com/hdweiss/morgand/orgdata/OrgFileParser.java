package com.hdweiss.morgand.orgdata;

import android.text.TextUtils;
import android.util.Pair;

import com.hdweiss.morgand.utils.PreferenceUtils;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrgFileParser {

    private final HashSet<String> excludedTags;
    private final RuntimeExceptionDao<OrgNode, Integer> nodeDao;

    private LineNumberReader reader;
    private ParseStack parseStack;
	private OrgFile orgFile;

	public OrgFileParser() {
        this.excludedTags = PreferenceUtils.getExcludedTags();
        this.nodeDao = OrgNode.getDao();
	}

    public void parse(File file, OrgFile orgFile, OrgNode parent) throws IOException {
        init(file, orgFile, parent);
        LineNumberReader reader = new LineNumberReader(new FileReader(file));
        parse(reader);
    }

    private void init(File file, OrgFile orgFile, OrgNode parent) {
        OrgNode rootNode = new OrgNode();
        rootNode.type = OrgNode.Type.Heading;
        rootNode.title = file.getName();
        rootNode.file = orgFile;
        rootNode.parent = parent;
        nodeDao.create(rootNode);

        orgFile.node = rootNode;
        OrgFile.getDao().update(orgFile);
        this.orgFile = orgFile;

        this.parseStack = new ParseStack();
        this.parseStack.add(0, rootNode, "");
    }


    public void parse(final LineNumberReader reader) throws IOException {
        this.reader = reader;
        nodeDao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                String currentLine;
                while ((currentLine = reader.readLine()) != null)
                    parseLine(currentLine);
                return null;
            }
        });
    }

    private void parseLine(String line) throws IOException {
        if (TextUtils.isEmpty(line))
            return;

        OrgNode.Type type = determineType(line);
        OrgNode node;
        switch(determineType(line)) {
            case Heading:
                node = getNodeFromHeading(line);
                break;

            case Drawer:
                node = getNodeFromDrawer(line);
                break;

            case Check:
            case Date:
            case OrgProperty:
                node = getNodeFromLine(type, line);
                break;

            case Body:
            default:
                node = getNodeFromBody(line);
                break;
        }
        if (node != null)
            nodeDao.create(node);
    }

    public static OrgNode.Type determineType(final String line) {
        if (line.startsWith("*"))
            return OrgNode.Type.Heading;

        if (line.startsWith("#+"))
            return OrgNode.Type.OrgProperty;

        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("- [ ]"))
            return OrgNode.Type.Check;

        if (trimmedLine.matches(".*<\\d{4}-\\d{2}-\\d{2}.*>.*"))
            return OrgNode.Type.Date;

        if (trimmedLine.matches("\\s*:\\w*:\\s*"))
            return OrgNode.Type.Drawer;

        return OrgNode.Type.Body;
    }

    private OrgNode getNodeFromLine(OrgNode.Type type, String line) {
        OrgNode node = new OrgNode();
        node.file = orgFile;
        node.parent = parseStack.getCurrentNode();
        node.title = line;
        node.type = type;
        node.lineNumber = reader.getLineNumber();

        node.inheritedTags = parseStack.getCurrentTags();
        return node;
    }


    private static final String headingRegex = "(.*?)" + // Title
            "\\s*" + // Whitespaces
            "(?::([^\\s]+):)?\\s*" + // Tags
            "$"; // End of line
    private static final Pattern headingPattern = Pattern.compile(headingRegex);
	private OrgNode getNodeFromHeading(final String line) {
        int starCount = numberOfStars(line);
		if (starCount == parseStack.getCurrentLevel()) { // Heading on same level
			parseStack.pop();
		} else if (starCount < parseStack.getCurrentLevel()) { // Heading on lower level
			while (starCount <= parseStack.getCurrentLevel())
				parseStack.pop();
		}

        Matcher matcher = headingPattern.matcher(line);
        matcher.find();
        final String heading = matcher.group(1);
        OrgNode node = getNodeFromLine(OrgNode.Type.Heading, heading);
        if (matcher.group(2) != null)
            node.tags = matcher.group(2);
		parseStack.add(starCount, node, "");
        return node;
    }

    private OrgNode getNodeFromBody(final String line) throws IOException {
        OrgNode node = getNodeFromLine(OrgNode.Type.Drawer, line);

        StringBuilder builder = new StringBuilder();
        builder.append(line);
        builder.append("\n");

        final ArrayList<OrgNode.Type> allowedTypes = new ArrayList<OrgNode.Type>();
        allowedTypes.add(OrgNode.Type.Body);
        node.title = readSection(builder, allowedTypes, null);

        return node;
    }

    private OrgNode getNodeFromDrawer(final String line) throws IOException {
        OrgNode node = getNodeFromLine(OrgNode.Type.Drawer, line);

        StringBuilder builder = new StringBuilder();
        builder.append(line);
        builder.append("\n");

        final ArrayList<OrgNode.Type> allowedTypes = new ArrayList<OrgNode.Type>();
        allowedTypes.add(OrgNode.Type.Drawer);
        allowedTypes.add(OrgNode.Type.Body);
        node.title = readSection(builder, allowedTypes, ":END:");

        return node;
    }


    private static final Pattern starPattern = Pattern.compile("^(\\**)\\s");
    public static int numberOfStars(final String thisLine) {
        Matcher matcher = starPattern.matcher(thisLine);
        if(matcher.find()) {
            return matcher.end(1) - matcher.start(1);
        } else
            return 0;
    }

    private String readSection(final StringBuilder builder, final ArrayList<OrgNode.Type> allowedTypes, final String endMarker) throws IOException {
        while (true) {
            reader.mark(Integer.MAX_VALUE);
            String currentLine = reader.readLine();

            if (currentLine == null)
                break;

            OrgNode.Type type = determineType(currentLine);
            if (allowedTypes.contains(type) == false) {
                reader.reset();
                break;
            }

            builder.append(currentLine);
            builder.append("\n");
            if (TextUtils.isEmpty(endMarker) == false && currentLine.trim() == endMarker)
                break;
        }

        return builder.toString();
    }


	private class ParseStack {
		private Stack<Pair<Integer, OrgNode>> parseStack;
		private Stack<String> tagStack;

		public ParseStack() {
			this.parseStack = new Stack<Pair<Integer, OrgNode>>();
			this.tagStack = new Stack<String>();
		}

		public void add(int level, OrgNode node, String tags) {
			parseStack.push(new Pair<Integer, OrgNode>(level, node));
			tagStack.push(stripTags(tags));
		}

		private String stripTags(String tags) {
			if (excludedTags == null || TextUtils.isEmpty(tags))
				return tags;

			StringBuilder result = new StringBuilder();
			for (String tag: tags.split(":")) {
				if (excludedTags.contains(tag) == false) {
					result.append(tag);
					result.append(":");
				}
			}

			if(!TextUtils.isEmpty(result))
				result.deleteCharAt(result.lastIndexOf(":"));

			return result.toString();
		}

		public void pop() {
			this.parseStack.pop();
			this.tagStack.pop();
		}

		public int getCurrentLevel() {
			return parseStack.peek().first;
		}

		public OrgNode getCurrentNode() {
			return parseStack.peek().second;
		}

		public String getCurrentTags() {
			return tagStack.peek();
		}
	}
}
