package course.java.sdm.engine.SDM;

import course.java.sdm.engine.Inventory.Inventory;
import course.java.sdm.engine.Inventory.InventoryItem;
import course.java.sdm.engine.Orders.Orders;
import course.java.sdm.engine.Orders.Order;
import course.java.sdm.engine.Orders.eOrderType;
import course.java.sdm.engine.Store.Store;
import course.java.sdm.engine.Orders.Cart;
import course.java.sdm.engine.Orders.CartItem;
import course.java.sdm.engine.jaxb.schema.generated.SDMItem;
import course.java.sdm.engine.jaxb.schema.generated.SDMSell;
import course.java.sdm.engine.jaxb.schema.generated.SDMStore;
import course.java.sdm.engine.jaxb.schema.generated.SuperDuperMarketDescriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class SDM {

    private String loadingErrorMessage="";
    private SuperDuperMarketDescriptor mySDM;
    private List<Store> stores;
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
                    createInventory(sdm);
                    createStores(sdm);
                    Order.setNumOfOrders(0);
                    inventory.updateStoresCarryingItems(stores);
                    inventory.updateAvePrice();
                    orderHistory = new Orders();
                    res = true;
                }
            return res;

        } catch (JAXBException e) {
            e.printStackTrace();
            loadingErrorMessage = loadingErrorMessage.concat("Encountered JAXException: course.java.sdm.engine.SDM from file is not valid");
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


    public boolean isSDMValidAppWise(SuperDuperMarketDescriptor sdm) {
        boolean areItemIdsUnique,areStoreIdsUnique,isStoreUsingUniqueItemIds, isStoreUsingExistingItemIds, isEachExistingItemSoldSomewhere, areLocationsLegal;

        List<SDMItem> sdmItems = sdm.getSDMItems().getSDMItem();
        List<SDMStore> sdmStores = sdm.getSDMStores().getSDMStore();

        //Since we expect no duplicates, we can store Item and Store ids in lists
        List<Integer> listOfItemIds = getListOfItemIds(sdmItems);
        List<Integer> listOfStoreIds = getListOfStoreIds(sdmStores);

        //error 3.2
        areItemIdsUnique = checkListOfIntsUnique(listOfItemIds, "course.java.sdm.engine.SDM-Items");
        //error 3.3
        areStoreIdsUnique = checkListOfIntsUnique(listOfStoreIds, "course.java.sdm.engine.SDM-Stores");
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
                loadingErrorMessage = loadingErrorMessage.concat("Error: Store-id= " + store.getId() + " has illegal location ("+x+", " + y +"). Coordinates must be between [1,50]");
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

    public void addNewStaticOrder(Store storeChoice, Order order) {
        storeChoice.addOrder(order);
        orderHistory.addOrder(order);
        inventory.updateSalesMap(order);
    }

    public void addNewDynamicOrder(Set <Store> storesBoughtFrom, Order order) {
        addSplittedOrdersToStores(storesBoughtFrom, order);
        orderHistory.addOrder(order);
        inventory.updateSalesMap(order);
    }

    private void addSplittedOrdersToStores(Set<Store> storesBoughtFrom, Order order) {
        storesBoughtFrom.forEach(store -> {
            Cart cartForStore = ExtractCartForStore(store, order);
            float deliveryCostForStore = store.getDeliveryCost(order.getUserLocation());

            Set<Store> storeForThisSubOrder = new HashSet<Store>();
            storeForThisSubOrder.add(store);
            Order orderForStore = new Order(order.getUserLocation(),
                                            order.getOrderDate(),
                                            deliveryCostForStore,
                                            cartForStore,
                                            storeForThisSubOrder,
                                            eOrderType.SPLITTED_DYNAMIC_ORDER);
            store.addOrder(orderForStore);
        });
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
        Comparator<Store> comparator = (store1, store2) -> store1.getMapItemToPrices().get(item.getInventoryItemId()).compareTo(store2.getMapItemToPrices().get(item.getInventoryItemId()));
        Set<Store> storesWithItem = inventory.getMapItemsToStoresWithItem().get(item);
        Store cheapestStore = Collections.min(storesWithItem, comparator);

        return cheapestStore;
    }



    public String canAddExistingItemToStore(String input, Store store) {
        Inventory inventory = getInventory();
        List<Integer> listItemsAllowedToAddToStore = inventory.getListOfItemsNotSoldByStore(store)
                .stream().map(item -> item.getInventoryItemId()).collect(Collectors.toList());

        String outputString = "";
        try{
            int id = Integer.parseInt(input);

            if (!inventory.getListOfInventoryItemIds().contains(id)){
                outputString = outputString.concat(String.format("Operation failed: No item with id=%s can be found in system!\n", input));
            }

            else if (!listItemsAllowedToAddToStore.contains(id)){
                outputString = outputString.concat(String.format("Operation failed: Store %s already contains an item with id=%s\n", store.getStoreName(), input)   );
            }
            return outputString;

        } catch (NumberFormatException e){
            return String.format("Error: Invalid input. %s does not correspond to a valid item-Id", input);
        }
    }


    public void addInventoryItemToStore(InventoryItem item, Store store, int price){
        store.addItemToStoreInventory(item, price);
        inventory.updateStoresCarryingItems(stores);
        inventory.updateAvePrice();
    }



    public String removeItemFromStore(String input, Store store) {
        Boolean canRemoveItem = true;
        Inventory inventory = getInventory();

        String outputString = "";
        try{
            int id = Integer.parseInt(input);
            if (!inventory.getListOfInventoryItemIds().contains(id)){
                outputString = outputString.concat("Remove failed: no existing item with item-Id=" + id + " found in inventory.\n");
                return outputString;
            }
            else{
                InventoryItem item = inventory.getInventoryItemById(id);
                Set<Store> storesWithItem = inventory.getMapItemsToStoresWithItem().get(item);

                if (!storesWithItem.contains(store)){
                    outputString = outputString.concat("Remove failed: Store " + store.getStoreName() + " does not sell item with id= " + id + ".\n");
                    canRemoveItem = false;
                }
                if (storesWithItem.contains(store) && storesWithItem.size() == 1){
                    outputString = outputString.concat("Remove failed: Store " + store.getStoreName() + " is currently the only store selling item " + id +"\n");
                    canRemoveItem = false;
                }

                if (canRemoveItem){
                    String s = String.format("\nItem %d was successfully BANISHED from store %s! MWA-HA-HA-HA!!!", item.getInventoryItemId(), store.getStoreName());

                    store.getInventoryItems().remove(item);
                    inventory.getMapItemsToStoresWithItem().get(item).remove(store);
                    inventory.updateAvePrice();
                    outputString = outputString.concat(s);
                }
                return outputString;
            }

        } catch (NumberFormatException e){
            outputString = outputString.concat("Error: Invalid input. " + input + " does not correspond to a valid item-Id");
            return outputString;
        }
    }

    public void updateItemPriceAtStore(InventoryItem chosenItem, Store storeChoice, int price) {
        storeChoice.getMapItemToPrices().put(chosenItem.getInventoryItemId(), price);
        inventory.updateAvePrice();
    }
}
