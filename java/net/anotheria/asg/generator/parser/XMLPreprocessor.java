package net.anotheria.asg.generator.parser;

import net.anotheria.asg.generator.util.IncludedDocuments;
import net.anotheria.util.IOUtils;
import net.anotheria.util.StringUtils;

import java.io.*;
import java.util.List;

/**
 * This class preprocess an xml file. Its mainly used to put together a splitted xml file.
 * @author another
 *
 */
@SuppressWarnings("ALL")
public final class XMLPreprocessor {

    /**
     * Include commands 'tag'.
     */
    private static final String INCLUDE_COMMAND = "@include:";

    /**
     * Loads a file from the disk. Include marks are replaced with the content of the included files.
     * @param f
     * @return
     * @throws IOException
     */
    public static String loadFile(File f, IncludedDocuments includedDocuments) throws IOException {
        if (!f.exists())
            throw new IOException("File doesn't exists: "+f.getAbsolutePath());
        int buffLineNumber = 0;
        String content = IOUtils.readFileAtOnceAsString(f.getAbsolutePath());

        boolean includeDocumentIsNull = true;
        if (includedDocuments != null){
            includeDocumentIsNull = false;
            if ( !includedDocuments.isListEmpty()){
                buffLineNumber = includedDocuments.getLastInsertLine();
            }
        }


        List<String> tags = StringUtils.extractTags(content, '<', '>');
        for (String t : tags){
            if (!t.startsWith("<!--"))
                continue;
            int indexOfIncludeCommand = t.indexOf(INCLUDE_COMMAND);
            if (indexOfIncludeCommand==-1)
                continue;
            int space = t.indexOf(' ', indexOfIncludeCommand);
            String includeTarget = t.substring(indexOfIncludeCommand+INCLUDE_COMMAND.length(), space);
            File toInclude = new File(f.getParentFile()+File.separator+includeTarget);
            if (!toInclude.exists())
                throw new IOException("File to include doesn't exists: "+toInclude.getAbsolutePath()+" included from "+f.getAbsolutePath());

            if (!includeDocumentIsNull){
                int lineNumber = findLineOfIncludeCommand(content, includeTarget);
                if (lineNumber >= 0){
                    includedDocuments.setNewIncludedDocument(includeTarget,lineNumber+buffLineNumber);
                }
            }


            String includeContent = loadFile(toInclude ,includedDocuments);

            content = StringUtils.replaceOnce(content, t, includeContent);
        }

        return content;
    }

    /**
     * Finds line in document, where method going to put content of document whit name includeTarget
     * @param content current content of document
     * @param includeTarget name of file, that will pu
     * @return
     * @throws IOException
     */

    private static int findLineOfIncludeCommand(String content,String includeTarget) throws IOException{
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp-load",".xml");
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
            bw.write(content);
            bw.close();

            BufferedReader br = new BufferedReader(new FileReader(tempFile));
            String line = null;
            int countLine = -1;
            while ((line = br.readLine()) != null) {
                countLine++;
                if (line.contains(includeTarget)) {
                    br.close();
                    return countLine;
                }
            }
            return -1;
        } catch (FileNotFoundException e) {
            throw new IOException("File doesn't exist: "+e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }

    }
    /**
     * prevent initialization.
     */
    private XMLPreprocessor(){

    }
}
