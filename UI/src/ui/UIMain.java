package ui;


import SDM.SDM;
import jaxb.schema.generated.SDMItem;
import jaxb.schema.generated.SDMSell;
import jaxb.schema.generated.SDMStore;
import jaxb.schema.generated.SuperDuperMarketDescriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class UIMain {

    public static void main(String[] args) throws IOException {
        boolean fileExists = false;
        boolean isSDMLoaded = false;
        boolean wantsToQuit = false;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String s="";
        String xmlFileName = "";

        // create an object of Scanner class
        Scanner scanner = new Scanner(System.in);

        // ask user to enter operator
        String operator;

        SDM sdmInstance = new SDM();
        SuperDuperMarketDescriptor sdmLoaded;


        while (!wantsToQuit){
            showPossibleCommands(isSDMLoaded);
            operator = scanner.nextLine().toLowerCase();
            switch (operator){

                case "l":
                    String tmpString = readFilePath(bufferedReader);
                    if (tmpString != ""){
                        SuperDuperMarketDescriptor dummySDM;
                        System.out.println("User entered path: " + tmpString);

                        dummySDM = sdmInstance.getObjectFromXMLFile(tmpString);
                        if (SDM.isSDMValidAppWise(dummySDM)){
                            System.out.println("SDM-File loaded succesfully!");
                            sdmLoaded = dummySDM;
                            printDetailsOfSDM(sdmLoaded);
                        }

                    }
            }


        }
    }

    private static void showPossibleCommands(boolean fileSuccessfullyLoaded) {
        System.out.println("Press 'L' to load a new file, or 'Q' to quit at any time");

        if (fileSuccessfullyLoaded){
            System.out.println("1)Create a new student");
            System.out.println("2)details specific student");
            System.out.println("3)details of all students");
            System.out.println("4)details of the student age details");
            System.out.println("5)details of the student personal info");
        }
    }




    public static String readFilePath(BufferedReader bufferedReader) throws IOException {
        boolean isValidPath = false;
        String s;
        File f;
        do {
            System.out.println("Please enter a valid file path. Press 'c' to cancel command: ");
            s = bufferedReader.readLine();

            //System.out.println("Value of s: " + s);

            if (s.equalsIgnoreCase("c")){
                return "";
            }

            else if (s.length() <= 3 || !s.substring(s.length() - 3).toLowerCase().equals("xml")){
                System.out.println("Error: Invalid Path. Please enter full path to XML file");
            }

            else{
                f = new File(s);

                if (f.exists())
                    isValidPath = true;
            }
        } while (!isValidPath);
        return s;
    }

    private static void printDetailsOfSDM(SuperDuperMarketDescriptor sdm) {
        List<SDMItem> sdmItems = sdm.getSDMItems().getSDMItem();
        List<SDMStore> sdmStores = sdm.getSDMStores().getSDMStore();

        System.out.println("SDM-Items: [");
        for (SDMItem item: sdmItems){
            System.out.println("id= " + item.getId() + ", name= " + item.getName() + ", purchase Category = " + item.getPurchaseCategory() + ",");
        }

        System.out.println("\nSDM-Stores: [");
        for (SDMStore store: sdmStores){
            String loc = "[" + store.getLocation().getY() + ", " + store.getLocation().getY() + "]";
            String sells = "sells = [";
            for (SDMSell itemSold: store.getSDMPrices().getSDMSell()){
                //sells.concat("ajkbj;lkdjsfa;lkjdafd;sj");
                sells = sells.concat(" {item-id=" + itemSold.getItemId() + ", price=" + itemSold.getPrice() + "},");
            }
            sells = sells.concat("]");
            System.out.println("Store-Id=" + store.getId() + ", store-name= " + store.getName() + ", location= "+loc + ", " + sells );
        }
    }

}
