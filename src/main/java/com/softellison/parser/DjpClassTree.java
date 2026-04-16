package com.softellison.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public class DjpClassTree {

	private static final Set<String> setPackageName = new HashSet<>();

	public static void generateClassTree() throws IOException {
		ParserConfiguration config = new ParserConfiguration();
		config.setLanguageLevel(LanguageLevel.BLEEDING_EDGE);
		StaticJavaParser.setConfiguration(config);
		// TODO
		String srcMainJavaFolder = "C:\\develop\\workspaces\\eclipse-jee-2021-03-R-win32-x86_64-bnl\\writing_ja\\src\\main\\java";
		Path sourceRoot = Paths.get(srcMainJavaFolder);
		printProjectStructure(sourceRoot);
	}

	private static void printProjectStructure(Path sourceRoot) throws IOException {
		Files.walk(sourceRoot).filter(path -> path.toString().endsWith(".java")).sorted(Comparator.naturalOrder()).forEach(DjpClassTree::parseFile);
	}

	private static void parseFile(Path path) {
		try {
			CompilationUnit cu = StaticJavaParser.parse(path);
			String packageName = cu.getPackageDeclaration().map(pd -> pd.getName().toString()).orElse("(default package)");
			if (!setPackageName.contains(packageName)) {
				setPackageName.add(packageName);
				System.out.println("-p- " + packageName);
			}
			cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> printClass(clazz, "    -c- ")); // ├ 251c ├──
			// System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printClass(ClassOrInterfaceDeclaration clazz, String indent) {
		System.out.println(indent + clazz.getName());
		String memberIndent = indent.replace("-c- ", "") + "    ";
		// Fields
		List<FieldDeclaration> fields = clazz.getFields();
		for (FieldDeclaration field : fields) {
			// ├──
			field.getVariables().forEach(variable -> System.out.println(memberIndent + "-a- " + variable.getType() + " " + variable.getName()));
		}
		// Methods
		List<MethodDeclaration> methods = clazz.getMethods();
		for (MethodDeclaration method : methods) {
			String parameters = method.getParameters().stream().map(p -> p.getType() + " " + p.getName()).reduce((a, b) -> a + ", " + b).orElse("");
			// └──
			System.out.println(memberIndent + "-m- " + method.getName() + "(" + parameters + "): " + method.getType());
		}
		// Nested classes (recursive)
		// └──
		clazz.getMembers().stream().filter(m -> m instanceof ClassOrInterfaceDeclaration).map(m -> (ClassOrInterfaceDeclaration) m)
				.forEach(nested -> printClass(nested, memberIndent + " - "));
	}

}
