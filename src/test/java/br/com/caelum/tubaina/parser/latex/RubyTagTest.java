package br.com.caelum.tubaina.parser.latex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import br.com.caelum.tubaina.parser.SimpleIndentator;
import br.com.caelum.tubaina.resources.ResourceLocator;

public class RubyTagTest {
	private final String BEGIN = "{\n" + "\\small \\noindent \\ttfamily \n";
	private final String END = "\n}\n";
	private final RubyTag rubyTag = new RubyTag(new SimpleIndentator());

	@Test
	public void testComments() {
		String code = "# this is a ruby comment\n";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubycomment \\#~this~is~a~ruby~comment"
				+ END, result);
		code = "=begin\nThis is a\nmultiline comment\n=end";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubycomment \\verb#=#begin\\\\\nThis~is~a\\\\\nmultiline~comment\\\\\n\\verb#=#end"
				+ END, result);
	}

	@Test
	public void testSingleQuotedString() {
		String code = "\"this is a double quoted string\"";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubystring \\verb#\"#this~is~a~double~quoted~string\\verb#\"#"
				+ END, result);
	}

	@Test
	public void testDoubleQuotedString() {
		String code;
		String result;
		code = "'this is a single quoted string'";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubystring \\verb#'#this~is~a~single~quoted~string\\verb#'#"
				+ END, result);
	}

	@Test
	public void testGeneralizedSingleQuotedString() {
		String code;
		String result;
		code = "%q!I said, \"You said, 'She said it.'\"!";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubystring \\%q!I~said,~\\verb#\"#You~said,~\\verb#'#She~said~it.\\verb#'#\\verb#\"#!"
				+ END, result);
	}

	@Test
	public void testGeneralizedDoubleQuotedString() {
		String code;
		String result;
		code = "%Q/I said, \"You said, 'She said it.'\"/";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubystring \\%Q\\verb#/#I~said,~\\verb#\"#You~said,~\\verb#'#She~said~it.\\verb#'#\\verb#\"#\\verb#/#"
				+ END, result);
		code = "%(I said, \"You said, 'She said it.'\")";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubystring \\%(I~said,~\\verb#\"#You~said,~\\verb#'#She~said~it.\\verb#'#\\verb#\"#)"
				+ END, result);
	}

	@Test
	public void testQuotedTextInsideCommentIsNotString() {
		String code = "# this is a comment with 'quoted' and \"double quoted\" text";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubycomment \\#~this~is~a~comment~with~\\verb#'#quoted\\verb#'#~and~\\verb#\"#double~quoted\\verb#\"#~text"
				+ END, result);
	}

	@Test
	public void testMultipleStringsInSameLine() {
		String code = "method 'a string', \"another string\", %q/yet another string/";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubynormal method~\\rubystring \\verb#'#a~string\\verb#'#\\rubynormal ,~\\rubystring \\verb#\"#another~string\\verb#\"#\\rubynormal ,~\\rubystring \\%q\\verb#/#yet~another~string\\verb#/#"
				+ END, result);
	}

	@Test
	public void testDoubleQuotedTextInsideSingleQuotedString() {
		String code = "'this is a \"single-quoted\" string'";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubystring \\verb#'#this~is~a~\\verb#\"#single\\verb#-#quoted\\verb#\"#~string\\verb#'#"
				+ END, result);
	}
	
	@Test
	public void testLineOrientedStringLiteral() {
		String code = "puts <<BLAH\nThis is a\nmultiline, line-oriented\nstring\nBLAH";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubynormal puts~\\rubystring \\verb#<#\\verb#<#BLAH\\\\\nThis~is~a\\\\\nmultiline,~line\\verb#-#oriented\\\\\nstring\\\\\nBLAH\\\\"
				+ END, result);
		code = "<<`CODE`\necho \"hello\"\necho \"world\"\nCODE";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubystring \\verb#<#\\verb#<#`CODE`\\\\\necho~\\verb#\"#hello\\verb#\"#\\\\\necho~\\verb#\"#world\\verb#\"#\\\\\nCODE\\\\"
				+ END, result);
	}

	@Test
	public void testSingleQuotedTextInsideDoubleQuotedString() {
		String code = "puts \"He said: 'Hello World!'\"";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubynormal puts~\\rubystring \\verb#\"#He~said:~\\verb#'#Hello~World!\\verb#'#\\verb#\"#"
				+ END, result);
	}

	@Test
	public void testReservedWords() {
		String code = "BEGIN class ensure nil self when END def false not super while alias defined for or then yield and do if redo true begin else in rescue undef break elsif module retry unless case end next return until raise defined?";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN
				+ "\\rubykeyword BEGIN~\\rubykeyword class~"
				+ "\\rubykeyword ensure~\\rubykeyword nil~"
				+ "\\rubykeyword self~\\rubykeyword when~"
				+ "\\rubykeyword END~\\rubykeyword def~"
				+ "\\rubykeyword false~\\rubykeyword not~"
				+ "\\rubykeyword super~\\rubykeyword while~"
				+ "\\rubykeyword alias~\\rubykeyword defined~"
				+ "\\rubykeyword for~\\rubykeyword or~"
				+ "\\rubykeyword then~\\rubykeyword yield~"
				+ "\\rubykeyword and~\\rubykeyword do~"
				+ "\\rubykeyword if~\\rubykeyword redo~"
				+ "\\rubykeyword true~\\rubykeyword begin~"
				+ "\\rubykeyword else~\\rubykeyword in~"
				+ "\\rubykeyword rescue~\\rubykeyword undef~"
				+ "\\rubykeyword break~\\rubykeyword elsif~"
				+ "\\rubykeyword module~\\rubykeyword retry~"
				+ "\\rubykeyword unless~\\rubykeyword case~"
				+ "\\rubykeyword end~\\rubykeyword next~"
				+ "\\rubykeyword return~\\rubykeyword until~"
				+ "\\rubykeyword raise~\\rubykeyword defined?"
				+ END, result);
	}
	
	@Test
	public void testRegularExpressions() {
		String code = "/regexp/";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyregexp \\verb#/#regexp\\verb#/#" + END, result);
		code = "%r(another/regexp/)";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyregexp \\%r(another\\verb#/#regexp\\verb#/#)" + END, result);
		code = "/ReGeXp/i";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyregexp \\verb#/#ReGeXp\\verb#/#i" + END, result);
	}
	
	@Test
	public void testVariables() {
		String code = "@local = nil";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyvariable \\verb#@#local~\\rubynormal \\verb#=#~\\rubykeyword nil" + END, result);
		code = "@@count = 1";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyvariable \\verb#@#\\verb#@#count~\\rubynormal \\verb#=#~\\rubynumber 1" + END, result);
		code = "$counter++";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyvariable \\$counter\\rubynormal ++" + END, result);
	}
	
	@Test
	public void testConstant() {
		String code = "PI = 3.14159";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyconstant PI~\\rubynormal \\verb#=#~\\rubynumber 3.14159" + END, result);
	}

	@Test
	public void testNumbers() {
		String code = "12345";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynumber 12345" + END, result);
		code = "123.45";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynumber 123.45" + END, result);
		code = "123e45";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynumber 123e45" + END, result);
		code = "123E-45";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynumber 123E\\verb#-#45" + END, result);
		code = "-123.45e67";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynumber \\verb#-#123.45e67" + END, result);
		code = "0xffffff";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynumber 0xffffff" + END, result);
		code = "0b010010";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynumber 0b010010" + END, result);
		code = "01777";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynumber 01777" + END, result);
	}
	
	@Test
	public void testSymbols() {
		String code = "attr_accessor :name, :simple_symbol";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynormal attr\\_accessor~\\rubysymbol :name\\rubynormal ,~\\rubysymbol :simple\\_symbol" + END, result);
		code = "%s(strange_symbol)";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubysymbol \\%s(strange\\_symbol)" + END, result);
	}
	
	@Test
	public void testMultipleLineCode() throws IOException {
		ResourceLocator.initialize(".");
		String code = readFile("/src/test/resources/test_ruby_color.rb");
		String result = rubyTag.parse(code, ""); 
		String expected = readFile("/src/test/resources/test_ruby_color.tex");
		Assert.assertEquals(expected, result);
	}
	
	@Test
	public void testKeywordMethodCalledOnAString() {
		String code = "defined?(\"text\") # should return true";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubykeyword defined?\\rubynormal (\\rubystring \\verb#\"#text\\verb#\"#\\rubynormal )~\\rubycomment \\#~should~return~true" + END, result);
	}
	
	@Test
	public void testHashAndArrayKeys() {
		String code = "@params[:id]";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyvariable \\verb#@#params\\rubynormal [\\rubysymbol :id\\rubynormal ]" + END, result);
		code = "$instances[5]";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyvariable \\$instances\\rubynormal [\\rubynumber 5\\rubynormal ]" + END, result);
		code = "dict['word']";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynormal dict[\\rubystring \\verb#'#word\\verb#'#\\rubynormal ]" + END, result);
	}
	
	@Test
	public void testMultipleNamespaces() {
		String code = "class User < ActiveRecord::Base";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubykeyword class~\\rubyconstant User~\\rubynormal \\verb#<#~\\rubyconstant ActiveRecord\\rubynormal ::\\rubyconstant Base" + END, result);
	}
	
	@Test
	public void testNumbersInRangeWithoutSpacesBetween() {
		String code = "array = [1,2,3,4]";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynormal array~\\verb#=#~[\\rubynumber 1\\rubynormal ,\\rubynumber 2\\rubynormal ,\\rubynumber 3\\rubynormal ,\\rubynumber 4\\rubynormal ]" + END, result);
		code = "array = [1..4]";
		result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynormal array~\\verb#=#~[\\rubynumber 1\\rubynormal ..\\rubynumber 4\\rubynormal ]" + END, result);
	}
	
	@Test
	public void testEscapedCharactersInDoubleQuotedStrings() {
		String code = "\"this is a \\\"string\\\"\";";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubystring \\verb#\"#this~is~a~\\char92\\verb#\"#string\\char92\\verb#\"#\\verb#\"#\\rubynormal ;" + END, result);
	}
	
	@Test
	public void testEscapedCharactersInSingleQuotedStrings() {
		String code = "'this is a \\'string\\''";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubystring \\verb#'#this~is~a~\\char92\\verb#'#string\\char92\\verb#'#\\verb#'#" + END, result);
	}
	
	@Test
	public void testEscapedCharactersInRegularExpressions() {
		String code = "/\\/.*\\//";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyregexp \\verb#/#\\char92\\verb#/#.*\\char92\\verb#/#\\verb#/#" + END, result);
	}
	
	@Test
	public void testEscapedCharactersInGeneralizedStringsAndRegularExpressions() {
		String code = "%(this is a \\(generalized\\) string)";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubystring \\%(this~is~a~\\char92(generalized\\char92)~string)" + END, result);
	}
	
	@Test
	public void testKeywordInsideIdentifierIsNotKeyword() {
		String code = "Session.expects(:open).once.and_return(s)";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubyconstant Session\\rubynormal .expects(\\rubysymbol :open\\rubynormal ).once.and\\_return(s)" + END, result);
	}
	
	@Test
	public void testArithmeticExpression() {
		String code = "print \"I'm crazy!\" if (3+4)-2++10.8 = (3*2)/4";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynormal print~\\rubystring \\verb#\"#I\\verb#'#m~crazy!\\verb#\"#~\\rubykeyword if~\\rubynormal (\\rubynumber 3\\rubynormal +\\rubynumber 4\\rubynormal )\\verb#-#\\rubynumber 2\\rubynormal +\\rubynumber +10.8~\\rubynormal \\verb#=#~(\\rubynumber 3\\rubynormal *\\rubynumber 2\\rubynormal )\\verb#/#\\rubynumber 4" + END, result);
	}
	
	@Test
	public void testInterpolationInDoubleQuotedStringHasDifferentColor() {
		String code = "\"string with #{%Q!string with #{i+1+1} interpolation!}\"";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubystring \\verb#\"#string~with~\\#\\{\\rubynormal \\rubystring \\%Q!string~with~\\#\\{\\rubynormal i+\\rubynumber 1\\rubynormal +\\rubynumber 1\\rubystring \\}~interpolation!\\}\\verb#\"#" + END, result);
	}
	
	@Test
	public void testMultipleInterpolationsInsideDoubleQuotedString() {
		String code = "%/Today is #{@date.month} #{@date.day}#{suffixForDay(@date.day)}/";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubystring \\%\\verb#/#Today~is~\\#\\{\\rubynormal \\rubyvariable \\verb#@#date\\rubynormal .month\\rubystring \\}~\\#\\{\\rubynormal \\rubyvariable \\verb#@#date\\rubynormal .day\\rubystring \\}\\#\\{\\rubynormal suffixForDay(\\rubyvariable \\verb#@#date\\rubynormal .day)\\rubystring \\}\\verb#/#" + END, result);
	}
	
	@Test
	public void testLineOrientedStringWithMultipleIdentifiers() {
		String code = "print <<EXP1, <<'puts', <<EXP2\n#{params[:user].name}, write the code:\nEXP1\nputs \"#{params[:id]}\"\nputs\nto print the id\nEXP2";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynormal print~\\rubystring \\verb#<#\\verb#<#EXP1,~\\verb#<#\\verb#<#\\verb#'#puts\\verb#'#,~\\verb#<#\\verb#<#EXP2\\\\\n\\#\\{\\rubynormal params[\\rubysymbol :user\\rubynormal ].name\\rubystring \\},~write~the~code:\\\\\nEXP1\\\\\nputs~\\verb#\"#\\#\\{params[:id]\\}\\verb#\"#\\\\\nputs\\\\\nto~print~the~id\\\\\nEXP2\\\\" + END, result);
	}
	
	@Test
	public void testCommentsAmongTextBug() {
		String code = "print a.name\n# a comment\nb = 10";
		String result = rubyTag.parse(code, "");
		Assert.assertEquals(BEGIN + "\\rubynormal print~a.name\\\\\n\\rubycomment \\#~a~comment\\\\\n\\rubynormal b~\\verb#=#~\\rubynumber 10" + END, result);
	}
	
	private String readFile(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ResourceLocator.getInstance().getFile(filename)));
		String line;
		String content = "";
		while ((line = reader.readLine()) != null) {
			content += line + "\n";
		}
		reader.close();
		return content;
	}
}