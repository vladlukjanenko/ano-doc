package net.anotheria.asg.generator.util;

/**
 * Bean of included document.
 */
public class IncludeDocumentsBean {
    /**
     * Document name.
     */
    private String documentName;
    /**
     * Line, where document was included.
     */
    private int insertLine;

    public IncludeDocumentsBean(String documentName, int insertLine) {
        this.documentName = documentName;
        this.insertLine = insertLine;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public int getInsertLine() {
        return insertLine;
    }

    public void setInsertLine(int insertLine) {
        this.insertLine = insertLine;
    }


    @Override
    public String toString() {
        return "IncludeDocumentsBean{" +
                "documentName='" + documentName + '\'' +
                ", insertLine=" + insertLine +
                '}';
    }
}
