package net.anotheria.asg.generator.validation;

import net.anotheria.asg.generator.util.IncludeDocumentsBean;
import net.anotheria.asg.generator.util.IncludedDocuments;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLAgainstXSDErrorHandler extends DefaultHandler{

    private boolean hasErrors = false;

    private IncludedDocuments includedDocuments = null;

    public XMLAgainstXSDErrorHandler(IncludedDocuments includedDocuments) {
        this.includedDocuments = includedDocuments;
    }

    public void error (SAXParseException e) {
        setHasErrors(true);
        System.out.println(getErrorMessage("error",e,includedDocuments));
    }

    public void warning (SAXParseException e) {
        System.out.println(getErrorMessage("warning",e,includedDocuments));
    }

    public void fatalError (SAXParseException e) {
        System.out.println(getErrorMessage("fatal",e,includedDocuments));
        e.printStackTrace();
        System.exit(1);
    }

    public String getErrorMessage(String errorType,SAXParseException e, IncludedDocuments includedDocuments){
        String message = "Validating "+errorType+" : "+e.getMessage() + " in line : "+e.getLineNumber();
        if (includedDocuments == null){
            return message;
        }
        IncludeDocumentsBean idb = includedDocuments.getIncludeDocumentByLine(e.getLineNumber());
        if (idb == null){
            return message;
        }

        int lineWithError= e.getLineNumber() - idb.getInsertLine();

        message = "Validating "+errorType+" : "+e.getMessage() +" in document : "+ idb.getDocumentName()+" in line : "+lineWithError;
        return message;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}