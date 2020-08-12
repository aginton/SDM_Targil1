package SDM;

import Item.Item;
import Orders.Orders;
import Store.Store;
import jaxb.schema.generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class SDM {

    protected String loadingErrorMessage="";
    protected SuperDuperMarketDescriptor mySDM;
    protected List<SDMItem> sdmItems;
    protected List<SDMStore> sdmStores;
    protected Orders orderHistory;

    protected List<Store> stores;
    protected List<Item> items;

    public Orders getOrderHistory(){
        if (orderHistory == null)
            orderHistory = new Orders();
        return orderHistory;
    }

    public List<Store> getStores(){return stores;}
    public List<Item> getItems(){return items;}


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
                createStores(sdm);
                createItems(sdm);
                sdmItems = getListOfSDMItems();
                sdmStores = getListOfSDMStores();
                res = true;
            }

            return res;


        } catch (JAXBException e) {
            e.printStackTrace();
            loadingErrorMessage = loadingErrorMessage.concat("Encountered JAXException: SDM from file is not valid");
            //System.out.println("SDM from file is not valid");
            return false;
        }
    }

    private void createItems(SuperDuperMarketDescriptor sdm) {
        if (items == null)
            items = new ArrayList<Item>();

        sdm.getSDMItems().getSDMItem().stream().forEach(item->{
            items.add(new Item(item));
        });

        for (Item item: items){
            List<Store> carryingStores = stores.stream().filter(i-> i.getInventory().containsKey(item.getItemId())).collect(Collectors.toList());
            float tmpSum = 0;

            for (Store s: carryingStores){
                item.getStoresSellingItem().add(s);
                int price = (Integer) s.getInventory().get(item.getItemId()).get("price");
                tmpSum += price;
            }
            item.setAvePrice(tmpSum/carryingStores.size());
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

            HashMap<Integer, HashMap<String, Object>> inventory = new HashMap<>();

            for (SDMSell sell: store.getSDMPrices().getSDMSell()){
                HashMap<String, Object> sellDetails = new HashMap<>();
                SDMItem matchingItem = sdm.getSDMItems().getSDMItem().stream().filter(item->item.getId()==sell.getItemId()).findFirst().get();

                //matchingItem shouldn't be null in any case
                if (matchingItem != null){
                    sellDetails.put("itemName", matchingItem.getName());
                    sellDetails.put("purchaseCategory", matchingItem.getPurchaseCategory());
                    sellDetails.put("price", sell.getPrice());
                    sellDetails.put("amountSold", 0);
                    inventory.put(sell.getItemId(),sellDetails);
                }
            }

            Store newStore = new Store(store.getId(), store.getName(), storeLoc, store.getDeliveryPpk(), inventory);
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

    public List<Integer> getListOfPriceIdsForStore(SDMStore store) {
        List<SDMSell> itemsSold = store.getSDMPrices().getSDMSell();
        List<Integer> res = new ArrayList<>();

        for (SDMSell item: itemsSold){
            res.add(item.getItemId());
        }
        return  res;
    }

    public String getNameOfProductById(int id){
        try{
            String res = getListOfSDMItems().stream().filter(item -> item.getId()==id).findFirst().get().getName();
            return res;
        } catch(NoSuchElementException e){
            return "";
        }
    }

    public SDMItem getSDMItemById(int id){
        return sdmItems.stream().filter(item -> item.getId()==id).findFirst().orElse(null);
    }

    public SDMSell getSDMSellById(SDMStore store, int id){
        return store.getSDMPrices().getSDMSell().stream().filter(item->item.getItemId() == id).findFirst().orElse(null);
    }

    public int getPriceOfItemAtStore(int id, SDMStore store){
        try{
            return store.getSDMPrices().getSDMSell().stream().filter(item->item.getItemId() == id).findFirst().get().getPrice();
        } catch (NoSuchElementException e){
            System.out.println("No such element found!");
            return -1;
        }
    }

    public String getNameByItemId(int id){
        try{
            return getListOfSDMItems().stream().filter(item-> item.getId() == id).findFirst().get().getName();
        } catch (NoSuchElementException e){
            System.out.println("Could not find name - No such element found!");
            return "-1";
        }
    }

    /*
    Returns list in format {itemId, itemName, price, purchaseCategory}
     */
    public Map<String, Object> getFullDetailsForItem(SDMSell itemSold, int storeId){
        SDMItem item;
        String itemName;
        item = sdmItems.stream().filter(i -> i.getId() == itemSold.getItemId()).findAny().orElse(null);

        Map<String, Object> itemMap = new HashMap<String, Object>();
        itemMap.put("itemId", item.getId());
        itemMap.put("itemName", item.getName());
        itemMap.put("PurchaseCategory", item.getPurchaseCategory());
        itemMap.put("Price", itemSold.getPrice());

        return itemMap;
    }

    /*
    Input: int itemId
    Returns Map<k,v> of stores that sell item with id=itemId

     k = int StoreId
     v =  int price at store k
     */
    public Map<Integer, Integer> getMapForStoresThatSellItem(int itemId){
        Map<Integer, Integer> res = new HashMap<>();

        for (SDMStore store: getListOfSDMStores()){
            List<SDMSell> sells = store.getSDMPrices().getSDMSell();

            SDMSell sold = sells.stream().filter(item->item.getItemId() == itemId).findFirst().orElse(null);
            System.out.println("For store " + store.getId() + " and itemId= " + itemId +", is Object sold  null? :" + (sold == null));

            if (sold != null){
                res.put(store.getId(), sold.getPrice());
            }
        }
        return res;
    }

    public int getPriceForItemAtStore(int itemId, SDMStore store){
        int res = -1;
        SDMSell item = store.getSDMPrices().getSDMSell().stream().filter(i -> i.getItemId() == itemId).findFirst().orElse(null);

        if (item != null){
            res = item.getPrice();
        }
        return res;
    }






    public String showStoreInformation(SDMStore store){
        //String res = "";
        String str1 = "\nStore-Id= " + store.getId() + ", name= " + store.getName();
        String str2 = "";

        //String str2 =  " {item-id=" + itemSold.getItemId() + ", price=" + itemSold.getPrice() + "},"
        for (SDMSell itemSold: store.getSDMPrices().getSDMSell()){

            //TODO: Make more efficient
            String itemName = "";
            for (SDMItem item: sdmItems){
                int itemNum = itemSold.getItemId();

                if (itemNum == item.getId()){
                    str2 = str2.concat("\n\t{item-id=" + itemNum + ", name= " + item.getName() + ", PurchaseCategory= " + item.getPurchaseCategory() + "price= " + itemSold.getPrice());
                    str2 = str2.concat(", Quantity ordered= ");
                }

            }
        }
        List<Integer> storeLoc = getStoreLocation(store);
        String str3 = "\nlocation=" + "(" + storeLoc.get(0) + ", " + storeLoc.get(1) + "), PPK= " + store.getDeliveryPpk();
        String res = str1 + ", " + str2 + str3;
        return res;
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
                //System.out.println("Error: Item with id= " + existingItem + " is not sold in any store.");
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
                    //System.out.println("Error: Store-Id = " + store.getId() + " is selling multiple items with id =" + sold.getItemId());
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
                //System.out.println("Error: id=" + num + " is not unique for type " + problematicType + "\n");

                res = false;
            }
        }
        return res;
    }

}
