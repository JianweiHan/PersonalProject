import net.sourceforge.plantuml.SourceStringReader;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Created by jhan on 9/28/15.
 */
public class UMLGenerator {
    public static void umlGenerator(Collection<String> classStrUML, Collection<String> associationStrUML, Collection<String> extendStrUML, Collection<String> ballsocketStrUML) throws Exception {
        OutputStream png = new FileOutputStream("test.png");
        String source = "@startuml\n";
        source +="title UML Diagram CMPE202\n";
        source +="skinparam classAttributeIconSize 0\n";
        source +="skinparam usecaseBackgroundColor #A80036\n";
        source +="skinparam usecaseBorderColor Transparent\n";
        source +="skinparam usecaseFontSize 1\n";
        source +="skinparam usecaseFontColor #A80036\n";

        for(String item:classStrUML)
        {
            source += item;
        }

        for(String item:associationStrUML)
        {
            source += item;
        }

        for(String item:extendStrUML)
        {
            source += item;
        }

        for(String item: ballsocketStrUML)
        {
            source += item;
        }

        source += "@enduml\n";


        //System.out.print(source);//print test




        SourceStringReader reader = new SourceStringReader(source);
// Write the first image to "png"
        String desc = reader.generateImage(png);
// Return a null string if no generation
    }
}
