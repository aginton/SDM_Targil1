package SDM;

import course.java.sdm.engine.Inventory.Inventory;
import course.java.sdm.engine.Inventory.InventoryItem;
import course.java.sdm.engine.Orders.Orders;
import course.java.sdm.engine.Orders.Order;
import course.java.sdm.engine.Orders.eOrderType;
import course.java.sdm.engine.Store.Store;
import course.java.sdm.engine.Orders.Cart;
import course.java.sdm.engine.Orders.CartItem;
import jaxb.schema.generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;

public class SDM {

    private String loadingErrorMessage="";
    private SuperDuperMarketDescriptor mySDM;
    private List<Store> stores;
    // inventory and orderHistory are only created after validation
    private Inventory inventory;
    private Orders orderHistory;


    public Orders getOrderHistory(){ return orderHistory; }
    public Inventory getInventory(){return inventory;}
    public List<Store> getStores(){return stores;}


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

                if (isSDMValidAppWise(sdm)) {
                    mySDM = sdm;
                    //First create each inventory item
                    //System.out.println("About to create inventory");
                    createInventory(sdm);
                    //System.out.println("\nAll inventory items created!\n\n");

                    //System.out.println("About to create stores");
                    createStores(sdm);
                    //System.out.println("\nAll stores created!\n\n");
                    Order.setNumOfOrders(1);

                    inventory.updateStoresCarryingItems(stores);
                    inventory.updateAvePrice();

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
        inventory = new Inventory();

        for (SDMItem sdmItem: sdm.getSDMItems().getSDMItem()){
            InventoryItem item = new InventoryItem(sdmItem);
            inventory.addNewItemToInventory(item);
        }
    }

    public void createStores(SuperDuperMarketDescriptor sdm){
        stores = new ArrayList<Store>();

        for (SDMStore store: sdm.getSDMStores().getSDMStore()){
            List<Integer> storeLoc = new ArrayList<>();
            storeLoc.add(store.getLocation().getX());
            storeLoc.add(store.getLocation().getY());

            Store newStore = new Store(store);

            for (SDMSell sell: store.getSDMPrices().getSDMSell()){
                InventoryItem itemToAdd = inventory.getListInventoryItems().stream().filter(i->i.getInventoryItemId()==sell.getItemId()).findFirst().get();
                if (itemToAdd != null){
                    newStore.getInventoryItems().add(itemToAdd);
                }
            }

            stores.add(newStore);
        }
    }

    public boolean isXMLfileType(File file) {
        return file.getName().endsWith(".xml");
    }

    public boolean isSDMValidAppWise(SuperDuperMarketDescriptor sdm) {

        boolean areItemIdsUnique,areStoreIdsUnique,isStoreUsingUniqueItemIds, isStoreUsingExistingItemIds, isEachExistingItemSoldSomewhere, areLocationsLegal;

        List<SDMItem> sdmItems = sdm.getSDMItems().getSDMItem();
        List<SDMStore> sdmStores = sdm.getSDMStores().getSDMStore();

        //Since we expect no duplicates, we can store Item and course.java.sdm.engine.Store ids as lists
        List<Integer> listOfItemIds = getListOfItemIds(sdmItems);
        List<Integer> listOfStoreIds = getListOfStoreIds(sdmStores);

        //error 3.2
        areItemIdsUnique = checkListOfIntsUnique(listOfItemIds, "SDM-Items");
        //error 3.3
        areStoreIdsUnique = checkListOfIntsUnique(listOfStoreIds, "SDM-Stores");
        //error 3.4
        isStoreUsingExistingItemIds = checkItemsSoldExist(sdmStores, listOfItemIds);
        //error 3.5
        isEachExistingItemSoldSomewhere = checkEachExistingItemSoldSomewhere(sdmStores, listOfItemIds);
        //error 3.6
        isStoreUsingUniqueItemIds = checkStoreUsesUniqueItemIds(sdmStores);
        //error 3.7
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
            if (x < 1 || y < 1 || x > 50 || y > 50){
                loadingErrorMessage = loadingErrorMessage.concat("Error: course.java.sdm.engine.Store-id= " + store.getId() + " has illegal location ("+x+", " + y +"). Coordinates must be between [1,50]");
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
                    loadingErrorMessage = loadingErrorMessage.concat("Error: course.java.sdm.engine.Store-Id = "+ store.getId() + " has item with item-Id= " + sold.getItemId() + ", but no such id exists in SDMItems!");
                    //System.out.println("Error: course.java.sdm.engine.Store-Id = "+ store.getId() + " has item with item-Id= " + sold.getItemId() + ", but no such id exists in SDMItems!");
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
                    loadingErrorMessage = loadingErrorMessage.concat("Error: course.java.sdm.engine.Store-Id = " + store.getId() + " is selling multiple items with id =" + sold.getItemId());
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

    public void addNewStaticOrder(Store storeChoice, Order order) {
        //System.out.println("About to add order to storeChoice");
            storeChoice.addOrder(order);

        //System.out.println("Successfully added order to storeChoice!\n");

        //System.out.println("About to add order to orderHistory");
        orderHistory.addOrder(order);

        inventory.updateSalesMap(order);
        //System.out.println("Successfully added order to orderHistory!\n");
    }

    public void addNewDynamicOrder(Set <Store> storesBoughtFrom, Order order) {
        //System.out.println("About to add order to storeChoice");

        //Bought from multiple stores, add sub-orders to stores
        addSplittedOrdersToStores(storesBoughtFrom, order);
        //System.out.println("Successfully added order to storeChoice!\n");

        //System.out.println("About to add order to orderHistory");
        orderHistory.addOrder(order);

        inventory.updateSalesMap(order);
        //System.out.println("Successfully added order to orderHistory!\n");
    }

    private void addSplittedOrdersToStores(Set<Store> storesBoughtFrom, Order order) {

        storesBoughtFrom.forEach(store -> {
            Cart cartForStore = ExtractCartForStore(store, order);
            float deliveryCostForStore = calculateDeliveryCostForStore(store.getDeliveryPpk(),
                                                                       order.getUserLocation(),
                                                                       store.getStoreLocation());
            Set<Store> storeForThisSubOrder = new HashSet<Store>();
            storeForThisSubOrder.add(store);
            Order orderForStore = new Order(order.getUserLocation(),
                                            order.getOrderDate(),
                                            deliveryCostForStore,
                                            cartForStore,
                                            storeForThisSubOrder,
                                            eOrderType.DYNAMIC_ORDER);
            store.addOrder(orderForStore);
        });
    }

    private float calculateDeliveryCostForStore(int deliveryPpk, List<Integer> userLocation, List<Integer> storeLocation) {

        int xDelta = userLocation.get(0) -storeLocation.get(0);
        int yDelta = userLocation.get(1) -storeLocation.get(1);
        float distance = (float) Math.sqrt((xDelta*xDelta)+(yDelta*yDelta)) ;

        return distance*deliveryPpk;
    }

    private Cart ExtractCartForStore(Store store, Order order) {

        Cart cartForStore = new Cart();

        order.getCartForThisOrder().getCart().forEach((key,cartItem) -> {
            if (cartItem.getStoreBoughtFrom() == store) {
                cartForStore.add(cartItem);
            }
        });

        return cartForStore;
    }

    public void addInventoryItemToStore(InventoryItem item, Store store, int price){
        store.addItemToStoreInventory(item, price);
        inventory.updateStoresCarryingItems(stores);
        inventory.updateAvePrice();
    }

    public Cart findCheapestCartForUser(HashMap<InventoryItem, Float> mapItemsChosenToAmount) {

        Cart cart = new Cart();

        mapItemsChosenToAmount.forEach((item,amount) -> {
           Store cheapestStore = findCheapestStoreForItem(item);
           int cheapestPrice = cheapestStore.getMapItemToPrices().get(item.getInventoryItemId());
           CartItem cartItem = new CartItem(item, amount, cheapestPrice, cheapestStore);
           cart.add(cartItem);
        });

        return cart;
    }

    public Store findCheapestStoreForItem(InventoryItem item) {

//        Comparator<course.java.sdm.engine.Store> comparator = new Comparator<course.java.sdm.engine.Store>() {
//            @Override
//            public int compare(course.java.sdm.engine.Store o1, course.java.sdm.engine.Store o2) {
//                return o1.getMapItemToPrices().get(item.getInventoryItemId()).compareTo(o2.getMapItemToPrices().get(item.getInventoryItemId()));
//            }
//        };

        Comparator<Store> comparator = (store1, store2) -> store1.getMapItemToPrices().get(item.getInventoryItemId()).compareTo(store2.getMapItemToPrices().get(item.getInventoryItemId()));
        Set<Store> storesWithItem = inventory.getMapItemsToStoresWithItem().get(item);
        Store cheapestStore = Collections.min(storesWithItem, comparator);

        return cheapestStore;
    }

    public void removeItemFromStore(InventoryItem chosenItem, Store storeChoice) {
        storeChoice.getInventoryItems().remove(chosenItem);
        inventory.getMapItemsToStoresWithItem().get(chosenItem).remove(storeChoice);
        inventory.updateAvePrice();
    }
}
