package com.hdweiss.morgand.orgdata;

import android.text.TextUtils;
import android.util.Pair;

import com.hdweiss.morgand.utils.PreferenceUtils;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrgFileParser {

    private final HashSet<String> excludedTags;
    private final RuntimeExceptionDao<OrgNode, Integer> nodeDao;

    private ParseStack parseStack;
	private StringBuilder payload;
	private OrgFile orgFile;

	public OrgFileParser() {
        this.excludedTags = PreferenceUtils.getExcludedTags();
        this.nodeDao = OrgNode.getDao();
	}

    public void parse(File file, OrgFile orgFile, OrgNode parent) throws IOException {
        init(file, orgFile, parent);
        BufferedReader reader = new BufferedReader(new FileReader(file));
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

        this.payload = new StringBuilder();
        this.parseStack = new ParseStack();
        this.parseStack.add(0, rootNode, "");
    }


    public void parse(final BufferedReader breader) throws IOException {
        nodeDao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                String currentLine;
                while ((currentLine = breader.readLine()) != null)
                    parseLine(currentLine);
                return null;
            }
        });

        // Add payload to the final node
    }

    private void parseLine(String line) {
        if (TextUtils.isEmpty(line))
            return;

        OrgNode.Type type = determineType(line);
        switch(determineType(line)) {
            case Heading:
                parseHeading(line);
                break;

            case Body:
            case Drawer:
                break;

            case Check:
            case Date:
                createOrgNode(type, line);
                break;
        }
    }

    private OrgNode.Type determineType(final String line) {
        if (line.startsWith("*"))
            return OrgNode.Type.Heading;

        if (line.startsWith("- [ ]"))
            return OrgNode.Type.Check;

        if (line.startsWith("SCHEDULED:") || line.startsWith("DEADLINE:"))
            return OrgNode.Type.Date;

        return OrgNode.Type.Body;
    }

    private OrgNode createOrgNode(OrgNode.Type type, String line) {
        OrgNode node = new OrgNode();
        node.file = orgFile;
        node.parent = parseStack.getCurrentNode();
        node.title = line;
        node.type = type;

        node.inheritedTags = parseStack.getCurrentTags();
        nodeDao.create(node);
        return node;
    }

	private void parseHeading(final String line) {
        int starCount = numberOfStars(line);
		if (starCount == parseStack.getCurrentLevel()) { // Heading on same level
			parseStack.pop();
		} else if (starCount < parseStack.getCurrentLevel()) { // Heading on lower level
			while (starCount <= parseStack.getCurrentLevel())
				parseStack.pop();
		}

        OrgNode node = createOrgNode(OrgNode.Type.Heading, line);
		parseStack.add(starCount, node, "");
    }


	private static final Pattern starPattern = Pattern.compile("^(\\**)\\s");
	public static int numberOfStars(final String thisLine) {
		Matcher matcher = starPattern.matcher(thisLine);
		if(matcher.find()) {
			return matcher.end(1) - matcher.start(1);
		} else
			return 0;
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
