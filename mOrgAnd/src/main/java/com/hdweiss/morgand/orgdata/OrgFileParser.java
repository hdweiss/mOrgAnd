package com.hdweiss.morgand.orgdata;

import android.text.TextUtils;
import android.util.Pair;

import com.hdweiss.morgand.utils.OrgNodeUtils;
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

    private final RuntimeExceptionDao<OrgNode, Integer> nodeDao;

    private LineNumberReader reader;
    private ParseStack parseStack;
	private OrgFile orgFile;
    private final HashSet<String> excludedTags;

	public OrgFileParser() {
        this.excludedTags = PreferenceUtils.getExcludedTags();
        this.nodeDao = OrgNodeRepository.getDao();
	}

    public void parse(OrgFile orgFile) throws IOException {
        if (orgFile.node == null)
            throw new IllegalArgumentException("Received " + orgFile.path + " with null node");

        File file = new File(orgFile.path);
        LineNumberReader reader = new LineNumberReader(new FileReader(file));

        OrgFile.getDao().createOrUpdate(orgFile);
        this.nodeDao.createOrUpdate(orgFile.node);
        this.orgFile = orgFile;
        this.parseStack = new ParseStack(excludedTags);
        this.parseStack.add(0, orgFile.node);

        parse(reader);

        orgFile.lastModified = file.lastModified();
        OrgFile.getDao().update(orgFile);
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
            case Headline:
                node = getNodeFromHeadline(line);
                break;

            case Drawer:
                node = getNodeFromDrawer(line);
                break;

            case Setting:
                node  = getNodeFromSetting(line);
                break;

            case Checkbox:
                node = getNodeFromCheckbox(line);
                break;

            case Date:
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
            return OrgNode.Type.Headline;

        if (line.startsWith("#+"))
            return OrgNode.Type.Setting;

        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("- [ ]") || trimmedLine.startsWith("- [X]"))
            return OrgNode.Type.Checkbox;

        if (trimmedLine.matches(".*<\\d{4}-\\d{2}-\\d{2}.*>.*"))
            return OrgNode.Type.Date;

        if (trimmedLine.matches("\\s*:\\w*:\\s*(.+)?"))
            return OrgNode.Type.Drawer;

        return OrgNode.Type.Body;
    }

    private OrgNode getNodeFromLine(OrgNode.Type type, final String line) {
        OrgNode node = new OrgNode();
        node.file = orgFile;
        node.parent = parseStack.getCurrentNode();
        node.level = parseStack.getCurrentLevel() + 1;
        node.title = line;
        node.type = type;
        node.lineNumber = reader.getLineNumber();

        node.inheritedTags = parseStack.getCurrentTags();
        return node;
    }

    private OrgNode getNodeFromSetting(final String line) {
        if (line.startsWith("#+FILETAGS:")) {
            String tags = line.substring("#+FILETAGS:".length());
            orgFile.node.tags = OrgNodeUtils.combineTags(tags, "", new HashSet<String>());
            nodeDao.update(orgFile.node);
        }

        OrgNode node = getNodeFromLine(OrgNode.Type.Setting, line);
        return node;
    }

    private static final String headingRegex = "(.*?)" + // Title
            "\\s*" + // Whitespaces
            "(?::([^\\s]+):)?\\s*" + // Tags
            "$"; // End of line
    private static final Pattern headingPattern = Pattern.compile(headingRegex);
	private OrgNode getNodeFromHeadline(final String line) {
        int starCount = numberOfStars(line);
		if (starCount == parseStack.getCurrentLevel()) { // Headline on same level
			parseStack.pop();
		} else if (starCount < parseStack.getCurrentLevel()) { // Headline on lower level
			while (starCount <= parseStack.getCurrentLevel())
				parseStack.pop();
		}

        Matcher matcher = headingPattern.matcher(line);
        matcher.find();
        final String heading = matcher.group(1) == null ? "" : matcher.group(1).trim();
        OrgNode node = getNodeFromLine(OrgNode.Type.Headline, heading);
        node.level = starCount;
        if (matcher.group(2) != null)
            node.tags = matcher.group(2);
		parseStack.add(starCount, node);
        return node;
    }

    private OrgNode getNodeFromBody(final String line) throws IOException {
        OrgNode node = getNodeFromLine(OrgNode.Type.Body, line);

        StringBuilder builder = new StringBuilder();
        builder.append(line);

        final ArrayList<OrgNode.Type> allowedTypes = new ArrayList<OrgNode.Type>();
        allowedTypes.add(OrgNode.Type.Body);
        node.title = readSection(builder, allowedTypes, null);

        return node;
    }

    private OrgNode getNodeFromDrawer(final String line) throws IOException {
        OrgNode node = getNodeFromLine(OrgNode.Type.Drawer, line);

        StringBuilder builder = new StringBuilder();
        builder.append(line);

        final ArrayList<OrgNode.Type> allowedTypes = new ArrayList<OrgNode.Type>();
        allowedTypes.add(OrgNode.Type.Drawer);
        allowedTypes.add(OrgNode.Type.Body);
        node.title = readSection(builder, allowedTypes, ":END:");

        return node;
    }

    private OrgNode getNodeFromCheckbox(final String line) throws IOException {
        OrgNode node = getNodeFromLine(OrgNode.Type.Checkbox, line);

        StringBuilder builder = new StringBuilder();
        builder.append(line);

        final ArrayList<OrgNode.Type> allowedTypes = new ArrayList<OrgNode.Type>();
        allowedTypes.add(OrgNode.Type.Body);
        node.title = readSection(builder, allowedTypes, "");
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

            builder.append("\n").append(currentLine);
            if (endMarker != null && currentLine.trim().equals(endMarker))
                break;
        }

        return builder.toString();
    }


	private class ParseStack {
        private final HashSet<String> excludedTags;
		private Stack<Pair<Integer, OrgNode>> parseStack;
		private Stack<String> tagStack;

		public ParseStack(HashSet<String> excludedTags) {
			this.parseStack = new Stack<Pair<Integer, OrgNode>>();
			this.tagStack = new Stack<String>();
            this.excludedTags = excludedTags;
		}

		public void add(int level, OrgNode node) {
			parseStack.push(new Pair<Integer, OrgNode>(level, node));
            final String combinedTags = OrgNodeUtils.combineTags(node.tags, node.inheritedTags, excludedTags);
			tagStack.push(combinedTags);
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
