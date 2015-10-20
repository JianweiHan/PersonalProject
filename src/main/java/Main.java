import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {

        //find all .java files in the given folder
        String folderPath="../testUML/uml-parser-test-4/";
        ArrayList<String> filePaths= new ArrayList<String>(); // used to save all the files' path
        String fileStr;
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                fileStr=file.getName();
                if(fileStr.endsWith(".java") || fileStr.endsWith(".JAVA"))
                    filePaths.add(fileStr);
            }
        }



        UseJavaParser usejavaparser=new UseJavaParser();
        //1.find classNames, extends and implements in each .java file
        for(String filePath:filePaths) {
            FileInputStream in = new FileInputStream(folderPath+filePath);
            CompilationUnit cu = new CompilationUnit();
            cu= JavaParser.parse(in);
            in.close();

            new UseJavaParser.ClassVisitor().visit(cu, null);

            if(usejavaparser.isInterfaceClassVisitor)
                usejavaparser.interfaceNames.add(usejavaparser.nameClassVisitor);
            else
                usejavaparser.classNames.add(usejavaparser.nameClassVisitor);

            if(usejavaparser.extendClassVisitor!=null) {
                for (ClassOrInterfaceType item : usejavaparser.extendClassVisitor)
                {
                    UseJavaParser.ExtendItem newItem=usejavaparser.new ExtendItem();
                    newItem.subClassName = usejavaparser.nameClassVisitor;
                    newItem.superClassName = item.getName();
                    usejavaparser.extendItemList.add(newItem);
                }
            }

            if(usejavaparser.implementClassVisitor!=null) {
                for (ClassOrInterfaceType item : usejavaparser.implementClassVisitor)
                {
                    UseJavaParser.ImplementInterfaceItem newItem=usejavaparser.new ImplementInterfaceItem();
                    newItem.implementName = usejavaparser.nameClassVisitor;
                    newItem.interfaceName = item.getName();
                    usejavaparser.implementInterfaceList.add(newItem);
                }
            }

            /*
            System.out.println(usejavaparser.nameFieldVisitor);
            usejavaparser.nameFieldVisitor.clear();
            System.out.println(usejavaparser.modifierFieldVistor);
            System.out.println(usejavaparser.typeFieldVisitor);
            usejavaparser.modifierFieldVistor.clear();
            usejavaparser.typeFieldVisitor.clear();
            */
        }



        //2.create class UML string for each .java file
        for(String filePath:filePaths) {
            FileInputStream in = new FileInputStream(folderPath+filePath);
            CompilationUnit cu = new CompilationUnit();
            cu= JavaParser.parse(in);
            in.close();

            new UseJavaParser.ClassVisitor().visit(cu, null);
            new UseJavaParser.MethodVisitor().visit(cu, null);
            new UseJavaParser.FieldVisitor().visit(cu, null);
            new UseJavaParser.ConstructorVisitor().visit(cu,null);
            new UseJavaParser.VariableDecVisitor().visit(cu,null);


            //a. create UML string for both interface and normal class
            usejavaparser.createClassStrUML();
            //b.create UML string if it is a class, not an interface
//            if(usejavaparser.classNames.indexOf(usejavaparser.nameClassVisitor)>=0) {
//                usejavaparser.createClassStrUML();
//            }

            usejavaparser.clearTempStaticClass();
        }


        /**
         * screen print result for test
         */
//        for(UseJavaParser.ImplementInterfaceItem newItem:usejavaparser.implementInterfaceList)
//        {
//            System.out.println("implement: " + newItem.implementName + ":" + newItem.interfaceName);
//        }
//
//        for(UseJavaParser.ExtendItem newItem:usejavaparser.extendItemList)
//        {
//            System.out.println("extend: " + newItem.subClassName+ ":"+newItem.superClassName);
//        }
//        for(UseJavaParser.UseInterfaceItem newItem:usejavaparser.useInterfaceList)
//        {
//            System.out.println("useInterface: " + newItem.useName+ ":"+newItem.interfaceName);
//        }
//
//
        for (UseJavaParser.AssociationItem item:usejavaparser.associationItemMap)
        System.out.println("startName:"+item.startName+ " endname: "+item.endName+" attribute:"+item.attributeName+item.ifMultiple);




        //3.create association UML string for java classes
        usejavaparser.createAssociationStrUML();
        //4.create extend relation UML string between java classes
        usejavaparser.createExtendStrUML();
        //5.create ball and socket UML string
        usejavaparser.createInterfaceStrUML();


        /**
         * call UMLGenerator that translates String out put into plantUML
         */
        UMLGenerator.umlGenerator(usejavaparser.classStrUML, usejavaparser.associationStrUML, usejavaparser.extendStrUML,usejavaparser.interfaceStrUML);


        /**
         * plantUML out print for test
         */
//        for(String name:usejavaparser.classNames)
//            System.out.println(name);

        /**
         * test if interfaceNames isEmpty?
         */
        System.out.println(usejavaparser.interfaceNames.isEmpty());

    }
}
