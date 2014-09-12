/*
 *
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2013 anudeepgade@users.sourceforge.net
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package com.sqleo.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Trie{

	private class Node {
		private final char ch;
		/**
		 * Flag indicates that this node is the end of the string.
		 */
		private boolean end;
		private LinkedList<Node> children;
		public Node(char ch) {
			this.ch = ch;
		}
		public void addChild(Node node) {
			if (children == null) {
				children = new LinkedList<Node>();
			}
			children.add(node);
		}
		public Node getNode(char ch) {
			if (children == null) {
				return null;
			}
			for (Node child : children) {
				if (child.getChar() == ch) {
					return child;
				}
			}
			return null;
		}
		public char getChar() {
			return ch;
		}
		public List<Node> getChildren() {
			if (this.children == null) {
				return Collections.emptyList();
			}
			return children;
		}
		public boolean isEnd() {
			return end;
		}
		public void setEnd(boolean end) {
			this.end = end;
		}
	}

	private Node root;
	
	public Trie(){
		root = new Node(' ');
	}
	
	/**
	 * Searches for a strings that match the prefix.
	 *
	 * @param prefix - prefix
	 * @return - list of strings that match the prefix, or empty list of no matches are found.
	 */
	public List<String> getWordsForPrefix(String prefix) {
		if (prefix.length() == 0) {
			return Collections.emptyList();
		}
		Node node = getNodeForPrefix(root, prefix);
		if (node == null) {
			return Collections.emptyList();
		}
		List<LinkedList<Character>> chars = collectChars(node);
		List<String> words = new ArrayList<String>(chars.size());
		for (LinkedList<Character> charList : chars) {
			words.add(combine(prefix.substring(0, prefix.length() - 1), charList));
		}
		return words;
	}

	private String combine(String prefix, List<Character> charList) {
		StringBuilder sb = new StringBuilder(prefix);
		for (Character character : charList) {
			sb.append(character);
		}
		return sb.toString();
	}

	private Node getNodeForPrefix(Node node, String prefix) {
		if (prefix.length() == 0) {
			return node;
		}
		Node next = node.getNode(prefix.charAt(0));
		if (next == null) {
			return null;
		}
		return getNodeForPrefix(next, prefix.substring(1, prefix.length()));
	}

	private List<LinkedList<Character>> collectChars(Node node) {
		List<LinkedList<Character>> chars = new ArrayList<LinkedList<Character>>();

		if (node.getChildren().size() == 0) {
			chars.add(new LinkedList<Character>(Collections.singletonList(node.getChar())));
		} else {
			if (node.isEnd()) {
				chars.add(new LinkedList<Character>(Collections.singletonList(node.getChar())));
			}
			List<Node> children = node.getChildren();
			for (Node child : children) {
				List<LinkedList<Character>> childList = collectChars(child);
				for (LinkedList<Character> characters : childList) {
					characters.push(node.getChar());
					chars.add(characters);
				}
			}
		}
		return chars;
	}


	public void addWord(String word) {
		addWord(root, word);
	}

	private void addWord(Node parent, String word) {
		if (word.trim().length() == 0) {
			return;
		}
		Node child = parent.getNode(word.charAt(0));
		if (child == null) {
			child = new Node(word.charAt(0));
			parent.addChild(child);
		}
		if (word.length() == 1) {
			child.setEnd(true);
		} else {
			addWord(child, word.substring(1, word.length()));
		}
	}

//	public static void main(String[] args) {
//		Trie tree = new Trie();
//		tree.addWord("world");
//		tree.addWord("work");
//		tree.addWord("wolf");
//		tree.addWord("life");
//		tree.addWord("love");
//		System.out.println(tree.getWordsForPrefix("wo"));
//	}
}