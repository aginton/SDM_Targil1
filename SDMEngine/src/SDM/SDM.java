package SDM;

import Inventory.Inventory;
import Inventory.InventoryItem;
//import Item.Item;
import Orders.Orders;
import Orders.Order;
import Store.Store;
import jaxb.schema.generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;

public class SDM {

    protected String loadingErrorMessage="";
    protected SuperDuperMarketDescriptor mySDM;

    ////////////////////////////////////////////////
    // Used for validation part
    protected List<Store> stores;
    public List<Store> getStores(){return stores;}
    //
//    protected List<Item> items;
//    public List<Item> getItems(){return items;}
    /////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////////////
    // inventory and orderHistory are only created after validation
    protected Inventory inventory;
    protected Orders orderHistory;


    public Orders getOrderHistory(){ return orderHistory; }
    public Inventory getInventory(){return inventory;}


    /*
    this function should update mySDM if corresponding file is valid
     */
    public boolean tryLoadingSDMObjectFromFile(String fileName){
        try {
            boolean res = false;
            File file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(SuperDuperMarketDescriptor.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SuperDuperMarketDescriptor sdm = (SuperDuperMarketDescriptor) jaxbUnmarshaller.unmarshal(file);

            if (isSDMValidAppWise(sdm)){
                mySDM = sdm;
                //First create each inventory item
                System.out.println("About to create inventory");
                createInventory(sdm);
                System.out.println("\nAll inventory items created!\n\n");

                System.out.println("About to create stores");
                createStores(sdm);
                System.out.println("\nAll stores created!\n\n");
                inventory.getListInventoryItems().stream().forEach(i->i.updateAvePrice());
                orderHistory = new Orders();
                res = true;
            }

            return res;

        } catch (JAXBException e) {
            e.printStackTrace();
            loadingErrorMessage = loadingErrorMessage.concat("Encountered JAXException: SDM from file is not valid");
            return false;
        }
    }


    private void createInventory(SuperDuperMarketDescriptor sdm) {
        if (inventory == null)
            inventory = new Inventory();

        for (SDMItem sdmItem: sdm.getSDMItems().getSDMItem()){
            InventoryItem item = new InventoryItem(sdmItem);
            inventory.addNewItemToInventory(item);
        }
    }

    public void createStores(SuperDuperMarketDescriptor sdm){
        if (stores == null){
            stores = new ArrayList<Store>();
        }

        for (SDMStore store: sdm.getSDMStores().getSDMStore()){
            List<Integer> storeLoc = new ArrayList<>();
            storeLoc.add(store.getLocation().getX());
            storeLoc.add(store.getLocation().getY());

            Store newStore = new Store(store, storeLoc, store.getDeliveryPpk());

            List<InventoryItem> newStoreInventory = new ArrayList<InventoryItem>();

            for (SDMSell sell: store.getSDMPrices().getSDMSell()){
                InventoryItem itemToAdd = inventory.getListInventoryItems().stream().filter(i->i.getInventoryItemId()==sell.getItemId()).findFirst().get();
                if (itemToAdd != null){
                    itemToAdd.addCarryingStore(newStore);
                    newStoreInventory.add(itemToAdd);
                }
            }
            newStore.setStoreInventory(newStoreInventory);
            stores.add(newStore);
        }
    }


    public boolean isSDMValidAppWise(SuperDuperMarketDescriptor sdm) {

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
        areLocationsLegal = checkLocationsAreAllowed(sdmStores);

        return (areItemIdsUnique && areStoreIdsUnique && isStoreUsingExistingItemIds && isStoreUsingUniqueItemIds && isEachExistingItemSoldSomewhere && areLocationsLegal);
    }


    public String getLoadingErrorMessage() {return loadingErrorMessage;}
    public SuperDuperMarketDescriptor getSuperDuperMarketDescriptor(){return mySDM;}


    public List<SDMItem> getListOfSDMItems(){return mySDM.getSDMItems().getSDMItem();}
    public List<SDMStore> getListOfSDMStores(){return mySDM.getSDMStores().getSDMStore();}

    public List<List<Integer>> getListOfStoreLocations(List<SDMStore> sdmStores) {
        List<List<Integer>> res = new ArrayList<>();

        for (SDMStore store: sdmStores){
            res.add(getStoreLocation(store));
        }
        return res;
    }

    public List<Integer> getStoreLocation(SDMStore store){
        List<Integer> res = new ArrayList<>();
        res.add(store.getLocation().getX());
        res.add(store.getLocation().getY());
        return res;
    }

    public List<Integer> getListOfStoreIds(List<SDMStore> sdmStores) {
        List<Integer> res = new ArrayList<>();

        for (SDMStore store: sdmStores){
            res.add(store.getId());
        }
        return  res;
    }

    public List<Integer> getListOfItemIds(List<SDMItem> sdmItems) {
        List<Integer> res = new ArrayList<>();

        for (SDMItem item: sdmItems){
            res.add(item.getId());
        }
        return  res;
    }

    private boolean checkLocationsAreAllowed(List<SDMStore> listOfStores) {
        boolean res = true;
        int x,y;
        Set<List<Integer>> tmpSet = new HashSet<>();

        for (SDMStore store: listOfStores){
            List<Integer> tmpList = new ArrayList<>();
            x = store.getLocation().getX();
            y = store.getLocation().getY();
            if (x < 0 || y < 0 || x > 50 || y > 50){
                loadingErrorMessage = loadingErrorMessage.concat("Error: Store-id= " + store.getId() + " has illegal location ("+x+", " + y +"). Coordinates must be between [0,50]");
                res = false;
            }

            tmpList.add(x);
            tmpList.add(y);
            if (!tmpSet.add(tmpList)){
                loadingErrorMessage = loadingErrorMessage.concat("Error: Multiple stores have location (" + x + ", " + y + ").");
                res = false;
            }
        }
        return res;
        }


    public boolean checkEachExistingItemSoldSomewhere(List<SDMStore> sdmStores, List<Integer> listOfExistingItemIds) {
        boolean res = true;
        Set<Integer> itemsSold = new HashSet<>();

        for (SDMStore store: sdmStores){

            for (SDMSell sold: store.getSDMPrices().getSDMSell()){
                itemsSold.add(sold.getItemId());
            }
        }

        for (Integer existingItem: listOfExistingItemIds){
            if (!itemsSold.contains(existingItem)){
                loadingErrorMessage = loadingErrorMessage.concat("Error: Item with id= " + existingItem + " is not sold in any store.");
                res = false;
            }
        }
        return res;
    }

    public boolean checkItemsSoldExist(List<SDMStore> sdmStores, List<Integer> listOfAllowedIds) {
        boolean res = true;

        for (SDMStore store: sdmStores){
            List<SDMSell> itemsSold = store.getSDMPrices().getSDMSell();

            for (SDMSell sold: itemsSold){
                if (!listOfAllowedIds.contains(sold.getItemId())){
                    loadingErrorMessage = loadingErrorMessage.concat("Error: Store-Id = "+ store.getId() + " has item with item-Id= " + sold.getItemId() + ", but no such id exists in SDMItems!");
                    //System.out.println("Error: Store-Id = "+ store.getId() + " has item with item-Id= " + sold.getItemId() + ", but no such id exists in SDMItems!");
                    res = false;
                }
            }
        }
        return res;
    }

    public boolean checkStoreUsesUniqueItemIds(List<SDMStore> sdmStores) {
        boolean res = true;

        for (SDMStore store: sdmStores){
            List<SDMSell> itemsSold = store.getSDMPrices().getSDMSell();
            Set<Integer> tmpSet = new HashSet<>();

            for (SDMSell sold: itemsSold){
                if (!tmpSet.add(sold.getItemId())){
                    loadingErrorMessage = loadingErrorMessage.concat("Error: Store-Id = " + store.getId() + " is selling multiple items with id =" + sold.getItemId());
                    res = false;
                }
            }
        }

        return res;
    }


    public boolean checkListOfIntsUnique(List<Integer> inputList, String problematicType){
        boolean res = true;
        Set<Integer> tmpSet = new HashSet<>();

        for (Integer num: inputList){
            if (!tmpSet.add(num)){
                loadingErrorMessage = loadingErrorMessage.concat("Error: id=" + num + " is not unique for type " + problematicType + "\n");
                res = false;
            }
        }
        return res;
    }

    public void addNewOrder(Store storeChoice, Order order) {
        System.out.println("About to add order to storeChoice");
        storeChoice.addOrder(order);
        System.out.println("Successfully added order to storeChoice!\n");

        System.out.println("About to add order to orderHistory");
        orderHistory.addOrder(order);

        inventory.updateSalesMap(order.getCartForThisOrder().getCart());
        System.out.println("Successfully added order to orderHistory!\n");
    }
}
