package SDM;

import jaxb.schema.generated.SDMItem;
import jaxb.schema.generated.SDMSell;
import jaxb.schema.generated.SDMStore;
import jaxb.schema.generated.SuperDuperMarketDescriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SDM {



    public SuperDuperMarketDescriptor getObjectFromXMLFile(String fileName){
        System.out.println("\nFrom File to Object");

        try {

            File file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(SuperDuperMarketDescriptor.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SuperDuperMarketDescriptor sdm = (SuperDuperMarketDescriptor) jaxbUnmarshaller.unmarshal(file);
            //System.out.println(sdm);

            //printDetailsOfSDM(sdm);

            System.out.println("======================================================================\n");

            return sdm;

            //System.out.println("======================================================================\n");
            //System.out.println("Result of isSdmValid: " + isSDMValid);
            //return isSDMValidAppWise(sdm);

        } catch (JAXBException e) {
            e.printStackTrace();
            System.out.println("SDM from file is not valid");
            return null;
        }
    }

    public static boolean isFileValidAppWise(String fileName ) {
        System.out.println("\nFrom File to Object");

        try {

            File file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(SuperDuperMarketDescriptor.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SuperDuperMarketDescriptor sdm = (SuperDuperMarketDescriptor) jaxbUnmarshaller.unmarshal(file);
            //System.out.println(sdm);

            //printDetailsOfSDM(sdm);

            System.out.println("======================================================================\n");

            //boolean isSDMValid = isSDMValidAppWise(sdm);

            System.out.println("======================================================================\n");
            //System.out.println("Result of isSdmValid: " + isSDMValid);
            return isSDMValidAppWise(sdm);

        } catch (JAXBException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean isSDMValidAppWise(SuperDuperMarketDescriptor sdm) {

        boolean areItemIdsUnique,areStoreIdsUnique,isStoreUsingUniqueItemIds, isStoreUsingExistingItemIds, isEachExistingItemSoldSomewhere, areLocationsLegal;

        List<SDMItem> sdmItems = sdm.getSDMItems().getSDMItem();
        List<SDMStore> sdmStores = sdm.getSDMStores().getSDMStore();

        //Since we expect no duplicates, we can store Item and Store ids as lists
        List<Integer> listOfItemIds = getListOfItemIds(sdmItems);
        List<Integer> listOfStoreIds = getListOfStoreIds(sdmStores);


        areItemIdsUnique = checkListOfIntsUnique(listOfItemIds, "SDM-Items");

        areStoreIdsUnique = checkListOfIntsUnique(listOfStoreIds, "SDM-Stores");


        isStoreUsingUniqueItemIds = checkStoreUsesUniqueItemIds(sdmStores);


        isStoreUsingExistingItemIds = checkItemsSoldExist(sdmStores, listOfItemIds);


        isEachExistingItemSoldSomewhere = checkEachExistingItemSoldSomewhere(sdmStores, listOfItemIds);

//        System.out.println("areItemIdsUnique: " + areItemIdsUnique);
//        System.out.println("areStoreIdsUnique: " + areStoreIdsUnique);
//        System.out.println("isStoreUsingUniqueItemIds: " + isStoreUsingUniqueItemIds);
//        System.out.println("isStoreUsingExistingItemIds: " + isStoreUsingExistingItemIds);
//        System.out.println("isEachExistingItemSoldSomewhere: " + isEachExistingItemSoldSomewhere);


        areLocationsLegal = checkLocationsAreAllowed(sdmStores);


        return (areItemIdsUnique && areStoreIdsUnique && isStoreUsingExistingItemIds && isStoreUsingUniqueItemIds && isEachExistingItemSoldSomewhere);
    }

    public static boolean checkLocationsAreAllowed(List<SDMStore> sdmStores) {
        boolean res = true;

        for (SDMStore store: sdmStores){
            List<SDMSell> itemsSold = store.getSDMPrices().getSDMSell();
            Set<Integer> tmpSet = new HashSet<>();

            for (SDMSell sold: itemsSold){
                if (!tmpSet.add(sold.getItemId())){
                    System.out.println("Error: Store-Id = " + store.getId() + " is selling multiple items with id =" + sold.getItemId());
                    res = false;
                }
            }
        }

        return res;
    }

    public static boolean checkEachExistingItemSoldSomewhere(List<SDMStore> sdmStores, List<Integer> listOfExistingItemIds) {
        boolean res = true;
        Set<Integer> itemsSold = new HashSet<>();

        for (SDMStore store: sdmStores){

            for (SDMSell sold: store.getSDMPrices().getSDMSell()){
                itemsSold.add(sold.getItemId());
            }
        }

        for (Integer existingItem: listOfExistingItemIds){
            if (!itemsSold.contains(existingItem)){
                System.out.println("Error: Item with id= " + existingItem + " is not sold in any store.");
                res = false;
            }
        }
        return res;
    }

    public static boolean checkItemsSoldExist(List<SDMStore> sdmStores, List<Integer> listOfAllowedIds) {
        boolean res = true;

        for (SDMStore store: sdmStores){
            List<SDMSell> itemsSold = store.getSDMPrices().getSDMSell();

            for (SDMSell sold: itemsSold){
                if (!listOfAllowedIds.contains(sold.getItemId())){
                    System.out.println("Error: Store-Id = "+ store.getId() + " has item with item-Id= " + sold.getItemId() + ", but no such id exists in SDMItems!");
                    res = false;
                }
            }
        }
        return res;
    }

    public static boolean checkStoreUsesUniqueItemIds(List<SDMStore> sdmStores) {
        boolean res = true;

        for (SDMStore store: sdmStores){
            List<SDMSell> itemsSold = store.getSDMPrices().getSDMSell();
            Set<Integer> tmpSet = new HashSet<>();

            for (SDMSell sold: itemsSold){
                if (!tmpSet.add(sold.getItemId())){
                    System.out.println("Error: Store-Id = " + store.getId() + " is selling multiple items with id =" + sold.getItemId());
                    res = false;
                }
            }
        }

        return res;
    }

    public static List<Integer> getListOfStoreIds(List<SDMStore> sdmStores) {
        List<Integer> res = new ArrayList<>();

        for (SDMStore store: sdmStores){
            res.add(store.getId());
        }
        return  res;
    }

    public static List<Integer> getListOfItemIds(List<SDMItem> sdmItems) {
        List<Integer> res = new ArrayList<>();

        for (SDMItem item: sdmItems){
            res.add(item.getId());
        }
        return  res;
    }


    public static boolean checkListOfIntsUnique(List<Integer> inputList, String problematicType){
        boolean res = true;
        Set<Integer> tmpSet = new HashSet<>();

        for (Integer num: inputList){
            if (!tmpSet.add(num)){
                System.out.println("Error: id=" + num + " is not unique for type " + problematicType + "\n");

                res = false;
            }
        }
        return res;
    }

}
