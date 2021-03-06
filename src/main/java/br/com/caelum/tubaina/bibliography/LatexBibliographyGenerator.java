package br.com.caelum.tubaina.bibliography;

public class LatexBibliographyGenerator implements BibliographyGenerator {

	public String generateTextOf(Bibliography bibliography) {
		StringBuffer result = new StringBuffer();

		escapeInvalidChars(bibliography);

		for (BibliographyEntry entry : bibliography.getEntries()) {
			result.append("@" + entry.type + "{");
			result.append(entry.label + ",\n");
			addField(result, "author", entry.author);
			addField(result, "title", entry.title);
			addField(result, "year", entry.year);
			addField(result, "journal", entry.getJournal());
			addField(result, "howPublished", "\\url{" + entry.howPublished + "}");
			addField(result, "publisher", entry.publisher);
			result.append("}\n");
		}

		return result.toString();
	}

	private void escapeInvalidChars(Bibliography bibliography) {
		for (BibliographyEntry entry : bibliography.getEntries()) {
			if (entry.howPublished != null)
				entry.howPublished = entry.howPublished.replaceAll("\\_", "\\\\_");
			if (entry.title != null)
				entry.title = entry.title.replaceAll("\\_", "\\\\_");
		}
	}

	private void addField(StringBuffer result, String key, String value) {
		if (value != null)
			result.append(key + " = {" + value + "},\n");

	}
}
