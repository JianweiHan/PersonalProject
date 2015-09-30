import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

/**
 * Created by jhan on 9/28/15.
 */
public class UseJavaParser {
    //attributes of JavaParser
    static List<String> classNames;
    static List<String> interfaceNames;
    static List<AssociationItem> associationItemMap;  //find association and multiplicity
    static List<ExtendItem> extendItemList; //save all the extend relationship
    static List<UseInterfaceItem> useInterfaceList; //find ball and socket interface
    static List<ImplementInterfaceItem> implementInterfaceList;


    static List<String> classStrUML;  // string input to umlGenerator for generating UML class
    static List<String> associationStrUML; // string input to umlGenerator for generating association UML
    static List<String> extendStrUML; // string input to umlGenerator for generating extend relation
    static List<String> ballsocketStrUML; // string input ot umlGenerator for generating ball and socket form of interface

    class AssociationItem {
        String startName;
        String endName;
        String attributeName;
        boolean ifMultiple;
    }

    class ExtendItem {
        String superClassName;
        String subClassName;
    }
    class UseInterfaceItem {
        String interfaceName;
        String useName;
    }
    class ImplementInterfaceItem {
        String interfaceName;
        String implementName;
    }

    //1. used to save output from ClassVisitor
    static String nameClassVisitor;
    static boolean isInterfaceClassVisitor;
    static List<ClassOrInterfaceType> extendClassVisitor;
    static List<ClassOrInterfaceType> implementClassVisitor;

    //2. used to save output from MethodVisitor
    static List<String> nameMethodVisitor;
    static List<Integer> modifierMethodVisitor;
    static List<String> typeMethodVisitor;
    static List<List<Parameter>> parameterListMethodVisitor;

    //3. used to save output from FieldVisitor
    static List<String> nameFieldVisitor;
    static List<Integer> modifierFieldVistor;
    static List<String> typeFieldVisitor;


    UseJavaParser(){
        classNames = new ArrayList<String>();
        interfaceNames = new ArrayList<String>();

        associationItemMap = new ArrayList<AssociationItem>();
        extendItemList = new ArrayList<ExtendItem>();
        useInterfaceList = new ArrayList<UseInterfaceItem>();
        implementInterfaceList = new ArrayList<ImplementInterfaceItem>();

        classStrUML = new ArrayList<String>();
        associationStrUML = new ArrayList<String>();
        extendStrUML = new ArrayList<String>();
        ballsocketStrUML = new ArrayList<String>();

        extendClassVisitor = new ArrayList<ClassOrInterfaceType>();
        implementClassVisitor = new ArrayList<ClassOrInterfaceType>();

        nameMethodVisitor = new ArrayList<String>();
        modifierMethodVisitor = new ArrayList<Integer>();
        typeMethodVisitor = new ArrayList<String>();
        parameterListMethodVisitor = new ArrayList<List<Parameter>>();

        nameFieldVisitor = new ArrayList<String>();
        modifierFieldVistor = new ArrayList<Integer>();
        typeFieldVisitor = new ArrayList<String>();
    }


    //1. try to visit class and interface names
    public static class ClassVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {

            nameClassVisitor = n.getName();
            isInterfaceClassVisitor = n.isInterface();
            extendClassVisitor = n.getExtends();
            implementClassVisitor = n.getImplements();

            System.out.println("Class name is: " + n.getName());
            /*
            System.out.println(n.getEndLine());
            if(n.isInterface())
                intefaceNames.add(n.getName());
            else
                classNames.add(n.getName());
            //new ClassVisitor().visit(n.getMembers(), null);
            */
        }

    }


    //2. try to visit method in the class
    public static class MethodVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            modifierMethodVisitor.add(n.getModifiers());
            nameMethodVisitor.add(n.getName());
            typeMethodVisitor.add(n.getType().toString());
            parameterListMethodVisitor.add(n.getParameters());

        }
    }


    //3. visit attribute
    public static class FieldVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(FieldDeclaration n, Object arg) {
            typeFieldVisitor.add(n.getType().toString());
            nameFieldVisitor.add(n.getVariables().get(0).toString());
            modifierFieldVistor.add(n.getModifiers());
        }
    }


    //1. create class UML & save use of interfaces & save association
    public void createClassStrUML() {
        String source = "";
        source +="class " +  nameClassVisitor +" {\n";
        for (String field: nameFieldVisitor)
        {
            //1. create field string of class UML
            int index = nameFieldVisitor.indexOf(field);

            // if field has associations with other classes, then it will not be printed in the class UML, but put into associationItemMap
            String substr1="";
            if (typeFieldVisitor.get(index).indexOf('[')>=0) {
                substr1 += typeFieldVisitor.get(index).substring(0,typeFieldVisitor.get(index).indexOf('['));
            }
            else if(typeFieldVisitor.get(index).indexOf('<')>=0){
                substr1 += typeFieldVisitor.get(index).substring(typeFieldVisitor.get(index).indexOf('<')+1,typeFieldVisitor.get(index).indexOf('>'));
            }

            if(classNames.indexOf(typeFieldVisitor.get(index))>=0 || classNames.indexOf(substr1)>=0 ){
                AssociationItem associationItem = new AssociationItem();
                associationItem.startName=nameClassVisitor;

                if(substr1!="")
                    associationItem.endName=substr1;
                else
                    associationItem.endName=typeFieldVisitor.get(index);

                associationItem.attributeName=field;

                if(typeFieldVisitor.get(index).indexOf('[')>=0 || typeFieldVisitor.get(index).indexOf('<')>=0)
                    associationItem.ifMultiple=true;
                else
                    associationItem.ifMultiple=false;

                associationItemMap.add(associationItem);
            }
            else{

                if (ModifierSet.isPublic(modifierFieldVistor.get(index))){
                    String typefieldstr="";
                    if (typeFieldVisitor.get(index).indexOf('[')>=0) {
                        typefieldstr += typeFieldVisitor.get(index).substring(0,typeFieldVisitor.get(index).indexOf('['));
                        typefieldstr += "(*)";
                    }
                    else if(typeFieldVisitor.get(index).indexOf('<')>=0){
                        typefieldstr += typeFieldVisitor.get(index).substring(typeFieldVisitor.get(index).indexOf('<')+1,typeFieldVisitor.get(index).indexOf('>'));
                        typefieldstr += "(*)";
                    }
                    else {
                        typefieldstr +=typeFieldVisitor.get(index);
                    }
                    source += "+" + field + ":" + typefieldstr + "\n";
                }
            }

            //2.find if any use of interface in the field type, save to useInterfaceList
            for(String interfaceName:interfaceNames) {

                if (interfaceName.equals(substr1) || interfaceName.equals(typeFieldVisitor.get(index))) {
                    UseInterfaceItem useInterfaceItem = new UseInterfaceItem();
                    useInterfaceItem.interfaceName = interfaceName;
                    useInterfaceItem.useName = nameClassVisitor;

                    if(useInterfaceList.indexOf(useInterfaceItem)<0)
                        useInterfaceList.add(useInterfaceItem);
                }
            }
        }

        source += "__\n";

        for(String methodName:nameMethodVisitor)
        {
            int index = nameMethodVisitor.indexOf(methodName);
            if(ModifierSet.isPublic(modifierMethodVisitor.get(index))) {
                String parameterStr="";

                for(Parameter parameterSingle:parameterListMethodVisitor.get(index)) {
                    String[] parts=parameterSingle.toString().split(" ");
                    parameterStr += parts[1] + ":"+parameterSingle.getType();
                    if(parameterListMethodVisitor.get(index).indexOf(parameterSingle)+1!=parameterListMethodVisitor.get(index).size())
                        parameterStr +=",";
                }

                source += "+" + methodName + "("+ parameterStr +"):"+typeMethodVisitor.get(index)+"\n";
            }


            //find if any use of interface in parameters, save to useInterfaceList
            for(Parameter parameterSingle:parameterListMethodVisitor.get(index)) {
                String substr1="";
                String paramtertype = parameterSingle.getType().toString();

                if(paramtertype.indexOf('[')>=0) {
                    substr1 += paramtertype.substring(0, paramtertype.indexOf('['));
                }
                else if(paramtertype.indexOf('<')>=0) {
                    substr1 += paramtertype.substring(paramtertype.indexOf('<')+1,paramtertype.indexOf('>'));
                }
                else
                    substr1 +=paramtertype;


                for(String interfaceName:interfaceNames) {
                    if (interfaceName.equals(substr1)) {
                        UseInterfaceItem useInterfaceItem = new UseInterfaceItem();
                        useInterfaceItem.interfaceName = interfaceName;
                        useInterfaceItem.useName = nameClassVisitor;

                        if(useInterfaceList.indexOf(useInterfaceItem)<0)
                            useInterfaceList.add(useInterfaceItem);
                    }
                }
            }


            //find if any use of interface in return type, save to useInterfaceList
            String substr1="";
            String returntype=typeMethodVisitor.get(index);
            if(returntype.indexOf('[')>=0) {
                substr1 += returntype.substring(0, returntype.indexOf('['));
            }
            else if(returntype.indexOf('<')>=0) {
                substr1 += returntype.substring(returntype.indexOf('<')+1,returntype.indexOf('>'));
            }
            else
                substr1 +=returntype;

            for(String interfaceName:interfaceNames) {
                if (interfaceName.equals(substr1)) {
                    UseInterfaceItem useInterfaceItem = new UseInterfaceItem();
                    useInterfaceItem.interfaceName = interfaceName;
                    useInterfaceItem.useName = nameClassVisitor;

                    if(useInterfaceList.indexOf(useInterfaceItem)<0)
                        useInterfaceList.add(useInterfaceItem);
                }
            }

        }
        source +="}\n";

        classStrUML.add(source);
    }

    //2. create association UML
    public void createAssociationStrUML() {
        String source = "";
        while(!associationItemMap.isEmpty()){
            String class1 = associationItemMap.get(0).startName;
            String class2 = associationItemMap.get(0).endName;

            int i=0;
            for( ; i<associationItemMap.size(); i++) {
                if(associationItemMap.get(i).startName.equals(class2)) {
                    break;
                }
            }
            if(i<associationItemMap.size()) {
                if(associationItemMap.get(0).ifMultiple && associationItemMap.get(i).ifMultiple) {
                    source += class1+"\"*\"" + "--" + "\"*\"" + class2 +"\n";
                }
                else if(associationItemMap.get(0).ifMultiple) {
                    source += class1+"\"1\"" + "--" + "\"*\"" + class2 +"\n";
                }
                else if(associationItemMap.get(i).ifMultiple) {
                    source += class1+"\"*\"" + "--" + "\"1\"" + class2 +"\n";
                }
                else {
                    source += class1+"\"1\"" + "--" + "\"1\"" + class2 +"\n";
                }
                associationItemMap.remove(i);
                associationItemMap.remove(0);
            }
            else {
                if(associationItemMap.get(0).ifMultiple) {
                    if(associationItemMap.get(0).endName.toUpperCase().equals(associationItemMap.get(0).attributeName.toUpperCase())){
                        source += class1 + "-->" + "\"*\"" + class2 + "\n";
                    }
                    else {
                        source += class1 + "-->" + "\"*\"" + class2 +":" + associationItemMap.get(0).attributeName + "\n";
                    }

                }
                else {
                    source += class1 + "-->" + "\"1\"" + class2 +":" + associationItemMap.get(0).attributeName + "\n";
                }
                associationItemMap.remove(0);
            }


        }

        associationStrUML.add(source);
    }


    //3. create extend relation UML
    public void createExtendStrUML() {
        String source = "";
        for(ExtendItem item: extendItemList){
            source += item.superClassName + "<|--" + item.subClassName+ "\n";
        }

        extendStrUML.add(source);

    }

    //4. create ball and socket UML
    public void createballsocketStrUML() {
        String source = "";
        int usecase1 = 0;
        int usecase2 = 1;

        while(!implementInterfaceList.isEmpty()){
            String interfaceName=implementInterfaceList.get(0).interfaceName;
            List<String> implementList=new ArrayList<String>();
            List<String> useList=new ArrayList<String>();

            int index = 0;
            while(index<implementInterfaceList.size()) {
                for(ImplementInterfaceItem implementItem:implementInterfaceList){
                    index++;
                    if(implementItem.interfaceName.equals(interfaceName)) {
                        implementList.add(implementItem.implementName);
                        implementInterfaceList.remove(implementItem);
                        index=0;
                        break;
                    }
                }
            }



            index = 0;
            while(index<useInterfaceList.size()) {
                for(UseInterfaceItem useItem:useInterfaceList) {
                    index++;
                    if(useItem.interfaceName.equals(interfaceName)) {
                        useList.add(useItem.useName);
                        useInterfaceList.remove(useItem);
                        index=0;
                        break;
                    }
                }
            }



            if(implementList.size()==1 && useList.size()==0) {
                source += interfaceName + "()-" + implementList.get(0)  +"\n";
            }

            else if(implementList.size()==1 && useList.size()==1) {
                source += implementList.get(0) + "-0)-" + useList.get(0) + ":`"+ interfaceName +"\n";
            }

            else if(implementList.size()==1 && useList.size() > 1) {
                source +="mix_usecase " + usecase1 + "\n";
                source += implementList.get(0) + "-0)- " + usecase1 +  ":`"+ interfaceName +"\n";
                for(String useName: useList) {
                    source += usecase1 + " -- " + useName + ":use\n";
                }
            }

            else if(implementList.size() > 1 && useList.size()==0) {
                source +="mix_usecase "+ usecase1 +"\n";
                source += interfaceName + "()- "+ usecase1 + "\n";
                for(String implementName:implementList) {
                    source += usecase1+" --" + implementName + "\n";
                }
            }

            else if(implementList.size() > 1 && useList.size() == 1) {
                source +="mix_usecase "+ usecase1 +"\n";
                source +=usecase1 + " -0)-" + useList.get(0) + ":`"+ interfaceName +"\n";
                for(String implementName:implementList) {
                    source += implementName + " -- "+ usecase1 +"\n";
                }
            }

            else if(implementList.size() > 1 && useList.size() > 1) {
                source +="mix_usecase "+ usecase1+ "\n";
                source +="mix_usecase "+ usecase2+ "\n";
                source += usecase1 + " -0)- "+ usecase2 +  ":`"+ interfaceName +"\n";

                for(String implementName:implementList) {
                    source += implementName + " -- "+ usecase1 +"\n";
                }

                for(String useName: useList) {
                    source +=usecase2 + " -- " + useName + ":use\n";
                }
            }

            usecase1=usecase1+2;
            usecase2=usecase2+2;

        }


        /*
        for(ImplementInterfaceItem implementItem:implementInterfaceList) {
            boolean used = false;


           for (UseInterfaceItem useItem:useInterfaceList) {
               if(implementItem.interfaceName.equals(useItem.interfaceName)){
                    source += implementItem.implementName + "-0)-" + useItem.useName+":`"+implementItem.interfaceName+"\n";
                   used= true;
               }

           }

            if(!used) {
                source += implementItem.interfaceName + "()-" + implementItem.implementName +"\n";
            }

        }
        */


        ballsocketStrUML.add(source);
    }



    public void clearTempStaticClass() {

        nameMethodVisitor.clear();
        modifierMethodVisitor.clear();
        typeMethodVisitor.clear();
        parameterListMethodVisitor.clear();

        nameFieldVisitor.clear();
        modifierFieldVistor.clear();
        typeFieldVisitor.clear();

    }


}
