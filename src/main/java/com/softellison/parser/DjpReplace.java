package com.softellison.parser;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class DjpReplace {

	public static void renameRecursively(String root, String search, String replace) throws IOException {
		renameRecursively(Paths.get(root), search, replace);
	}

	private static void renameRecursively(Path root, String search, String replace) throws IOException {
		try (Stream<Path> paths = Files.walk(root)) {
			// Rename children before parents
			paths.sorted(Comparator.reverseOrder()).forEach(path -> rename(path, search, replace));
		}
	}

	private static void rename(Path path, String search, String replace) {
		String name = path.getFileName().toString();
		if (!name.contains(search)) {
			return;
		}
		String newName = name.replace(search, replace);
		Path newPath = path.resolveSibling(newName);
		try {
			Files.move(path, newPath);
			System.out.println("Renamed: " + path + " -> " + newPath);
		} catch (IOException e) {
			System.err.println("Failed: " + path + " : " + e.getMessage());
		}
	}

}
