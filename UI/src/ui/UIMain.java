package ui;

import Inventory.Inventory;
import Inventory.InventoryItem;
import Inventory.ePurchaseCategory;
import Orders.Order;
import Orders.Cart;
import Orders.CartItem;
import SDM.SDM;
import Store.Store;
import jaxb.schema.generated.SDMItem;
import jaxb.schema.generated.SDMSell;
import jaxb.schema.generated.SDMStore;
import jaxb.schema.generated.SuperDuperMarketDescriptor;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIMain {

    public static void main(String[] args) throws IOException {
//        boolean fileExists = false;
        boolean isSDMLoaded = false;
        boolean wantsToQuit = false;
        SDM sdmInstance = new SDM();

//        SuperDuperMarketDescriptor sdmLoaded;

        // create an object of Scanner class
        Scanner scanner = new Scanner(System.in);

        // ask user to enter operator
        String operator;


        while (!wantsToQuit) {
            System.out.println("");
            showPossibleCommands(isSDMLoaded);
            operator = scanner.nextLine().toLowerCase().trim();
            switch (operator) {

                case "1":
                    String filePath = readFilePath();

                    if (!filePath.equalsIgnoreCase("q")) {
                        if (sdmInstance.tryLoadingSDMObjectFromFile(filePath)) {
                            System.out.println("SDM-File loaded successfully!");
                            isSDMLoaded = true;
                        } else {
                            System.out.println("\nUnable to load file:");
                            System.out.println(sdmInstance.getLoadingErrorMessage());
                        }
                    }
                    break;

                     // print information for stores in system
                case "2":
                    if (isSDMLoaded) {
                        viewAllStoresInSystem(sdmInstance);
                    }
                    break;

                    // display all items in system
                case "3":
                    if (isSDMLoaded){
                        viewAllItemsInSystem(sdmInstance);
                    }
                    break;

                    //add an order
                case "4":
                    if (isSDMLoaded) {
                        placeAnOrder(sdmInstance);
                    }

                    break;

                case "5":
                    if (isSDMLoaded){
                        viewOrderHistory(sdmInstance);
                    }
                    break;

                case "6":
                    if (isSDMLoaded){
                        updateInventoryForStore(sdmInstance);
                    }
                    break;

                case "q":
                    System.out.println("Goodbye!");
                    return;

                case "9":
                    //testThisMethod(sdmInstance);
                    break;

                default:
                    System.out.println("Invalid input. " + operator + " does not correspond to any command!");
            }


        }
    }

    /*This function has user select a store and then allows them to either
       1) add an already existing item to the store (if item not already in store inventory)
       2) update price of existing item in store inventory
       3) remove an item from store inventory (so long as other stores sell it)
     */
    private static void updateInventoryForStore(SDM sdmInstance) {
        Scanner in = new Scanner(System.in);
        Boolean isValidStoreChoice = false;
        Store storeChoice = null;
        List<Store> listOfStores = sdmInstance.getStores();
        List<Integer> listOfStoreIds = listOfStores.stream().map(i->i.getStoreId()).collect(Collectors.toList());

        //1. Show list of stores and ask user for id of store to update
        while (!isValidStoreChoice){
            System.out.println("Which store inventory do you wish to update? Please enter store-id from the following list, or enter 'cancel' to go back to main menu:");
            printAvailableStores(sdmInstance);
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("cancel"))
                return;

            isValidStoreChoice = checkIfInputIsAllowed(input, listOfStoreIds);

            if (isValidStoreChoice)
                storeChoice = listOfStores.get(Integer.parseInt(input) - 1);
        }

        //2. Ask user if they want to add new item, delete item, or update price for existing item for storeChoice
        while (true){
            System.out.println("\nStore: " + storeChoice.getStoreName());
            System.out.println("To add an existing item to this stores inventory, enter 'add'. ");
            System.out.println("To update price of item in this store's inventory, enter 'update'");
            System.out.println("To remove an item from store's inventory, enter 'remove'");
            System.out.println("When finished, enter 'done'");
            System.out.println("=======================================================================");

            String input = in.nextLine().trim().toLowerCase();
            switch (input){
                case "add":
                    addExistingItemToStoreInventory(sdmInstance, storeChoice);
                    break;

                case "update":
                    updatePricesForStore(sdmInstance, storeChoice);
                    break;

                case "remove":
                    removeItemFromStoreInventory(sdmInstance, storeChoice);
                    break;

                case "done":
                    return;

                default:
                    System.out.printf("Invalid input! %s does not correspond to a valid command ):\n", input);
            }

        }

        //3.
    }

    private static void updatePricesForStore(SDM sdmInstance, Store storeChoice) {
        Scanner in = new Scanner(System.in);
        Inventory inventory = sdmInstance.getInventory();
        InventoryItem chosenItem = null;
        Boolean isExistingItem=false, isSoldByStore = false, isValidPrice = false;
        int price = 0;

        List<Integer> existingInventoryItemIds = inventory.getListOfInventoryItemIds();
        List<Integer> itemsSoldAtStore = storeChoice.getInventoryItems().stream().map(item->item.getInventoryItemId()).collect(Collectors.toList());

        //0. Make sure store choice has items to sell
        if (itemsSoldAtStore.size() == 0){
            System.out.printf("\n%s is currently not selling any items!", storeChoice.getStoreName());
            return;
        }

        //1. Get item to update
        while (!isExistingItem || !isSoldByStore){
            System.out.printf("Which item's price do you wish to update? Enter Item-ID from the following list, or enter 'cancel' to go back to previous menu:\n", storeChoice.getStoreName());
            //TODO: (Maybe) make it so only items sold at storeChoice are shown
            printPriceTableForStore(sdmInstance, storeChoice);
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("cancel"))
                return;

            isExistingItem = checkIfInputIsAllowed(input, existingInventoryItemIds);

            //1.1. ID must correspond to existing item AND item not currently sold by store
            if (!isExistingItem){
                System.out.printf("Error: No item with id=%s can be found in system!\n", input);
            }
            else{
                isSoldByStore = checkIfInputIsAllowed(input, itemsSoldAtStore);
                if (!isSoldByStore){
                    System.out.printf("Error: Store %s does not sell item with id=%s\n", storeChoice.getStoreName(), input);
                }
                else {
                    chosenItem = inventory.getInventoryItemById(Integer.parseInt(input));
                }
            }
        }

        //TODO: fix code duplication
        //2. Update price
        while (!isValidPrice){
            System.out.printf("What price do you want to set for %s at store %s. Please enter a positive integer, or enter 'cancel' to go back to previous menu:\n", chosenItem.getItemName(), storeChoice.getStoreName());
            String input = in.nextLine().trim();

            if (input.equalsIgnoreCase("cancel"))
                return;

            if (checkIfInputIsAllowed(input, null)){
                price = Integer.parseInt(input);
                if (price > 0){
                    isValidPrice = true;
                }
                else
                    System.out.println("Error: Price cannot be a negative number!");
            }
        }

        storeChoice.getMapItemToPrices().put(chosenItem.getInventoryItemId(), price);
        sdmInstance.getInventory().updateAvePrice();
        System.out.printf("\nPrice of item %s at store %s was successfully changed to %d", chosenItem.getItemName(), storeChoice.getStoreName(), price);
    }

    private static void removeItemFromStoreInventory(SDM sdm, Store storeChoice){
        Scanner in = new Scanner(System.in);
        Inventory inventory = sdm.getInventory();
        InventoryItem chosenItem = null;
        Boolean isExistingItem=false, isSoldByStore = false;

        List<Integer> existingInventoryItemIds = inventory.getListOfInventoryItemIds();
        List<Integer> itemsSoldAtStore = storeChoice.getInventoryItems().stream().map(item->item.getInventoryItemId()).collect(Collectors.toList());

        //0. Make sure store choice has items to sell
        if (itemsSoldAtStore.size() == 0){
            System.out.printf("\n%s is currently not selling any items!", storeChoice.getStoreName());
            return;
        }

        //1. Get item to update
        while (!isExistingItem || !isSoldByStore){
            System.out.printf("Enter ID of item that is to be BANISHED by %s! Please select from the following list, or enter 'cancel' to go back to previous menu:\n", storeChoice.getStoreName());
            //TODO: (Maybe) make it so only items sold at storeChoice are shown
            printPriceTableForStore(sdm, storeChoice);
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("cancel"))
                return;

            isExistingItem = checkIfInputIsAllowed(input, existingInventoryItemIds);
            isSoldByStore = checkIfInputIsAllowed(input, itemsSoldAtStore);

            //1.1. ID must correspond to existing item AND item not currently sold by store
            if (!isExistingItem){
                System.out.printf("Error: No item with id=%s can be found in system!\n", input);
            }
            else{
                if (!isSoldByStore){
                    System.out.printf("Error: Store %s does not sell item with id=%s\n", storeChoice.getStoreName(), input);
                }
                else {
                    chosenItem = inventory.getInventoryItemById(Integer.parseInt(input));
                }
            }
        }

        if (inventory.getMapItemsToStoresWithItem().get(chosenItem).size() == 1){
            System.out.printf("\nCannot perform remove: Store %s is currently the only store selling item-id %d (%s)",
                    storeChoice.getStoreName(), chosenItem.getInventoryItemId(), chosenItem.getItemName());
            return;
        }

        sdm.removeItemFromStore(chosenItem, storeChoice);
        System.out.printf("\nItem %d was successfully BANISHED from store %s! MWA-HA-HA-HA!!!", chosenItem.getInventoryItemId(), storeChoice.getStoreName());

    }

    private static void addExistingItemToStoreInventory(SDM sdmInstance, Store storeChoice) {
        Scanner in = new Scanner(System.in);
        Inventory inventory = sdmInstance.getInventory();
        InventoryItem chosenItem = null;

        Boolean isExistingId=false, canBeAddedToStore = false, isValidPrice = false;
        int price = 0;

        List<Integer> existingInventoryItemIds = inventory.getListOfInventoryItemIds();
        List<Integer> listItemsAllowedToAddToStore = inventory.getListOfItemsNotSoldByStore(storeChoice)
                .stream().map(item->item.getInventoryItemId()).collect(Collectors.toList());


        //0. Make sure that there exists an item that can be added to storeChoice's inventory. If there isn't, return.
        if (listItemsAllowedToAddToStore.size() == 0){
            System.out.printf("No existing items can currently be added to store's inventory! %s already has everything!\n", storeChoice.getStoreName());
            return;
        }

        //1. Show list of existing items that are not currently sold by store
        while (!isExistingId || !canBeAddedToStore){
            System.out.printf("Enter Item-ID for item you wish to add to %s's inventory. Please select from the following list, or enter 'cancel' to go back to previous menu:\n", storeChoice.getStoreName());
            printItemsNotSoldByStore(sdmInstance, storeChoice);
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("cancel"))
                return;

            isExistingId = checkIfInputIsAllowed(input, existingInventoryItemIds);
            canBeAddedToStore = checkIfInputIsAllowed(input, listItemsAllowedToAddToStore);

            //1.1. Make sure item exists
            if (!isExistingId){
                System.out.printf("Error: No item with id=%s can be found in system!\n", input);
            }
            else{
                if (!canBeAddedToStore){
                    System.out.printf("Error: Store %s already contains an item with id=%s\n", storeChoice.getStoreName(), input);
                }
                else {
                    chosenItem = inventory.getInventoryItemById(Integer.parseInt(input));
                }
            }
        }

        //3. Get price to set for item
        while (!isValidPrice){
            System.out.printf("Enter price for %s at store %s? Please enter a positive integer, or enter 'cancel' to go back to previous menu:\n", chosenItem.getItemName(), storeChoice.getStoreName());
            String input = in.nextLine().trim();

            if (input.equalsIgnoreCase("cancel"))
                return;

            if (checkIfInputIsAllowed(input, null)){
                price = Integer.parseInt(input);
                if (price > 0){
                    isValidPrice = true;
                }
                else
                    System.out.println("Error: Price cannot be a negative number!");
            }
        }

        System.out.printf("Adding item %s (id=%d) to %s's inventory, at price=%d\n",
                chosenItem.getItemName(), chosenItem.getInventoryItemId(), storeChoice.getStoreName() ,price);

        sdmInstance.addInventoryItemToStore(chosenItem, storeChoice, price);
        System.out.printf("Current inventory for store %s\n", storeChoice.getStoreName());
        printPriceTableForStore(sdmInstance, storeChoice);
    }



    private static void printItemsNotSoldByStore(SDM sdmInstance, Store storeChoice) {
        //System.out.printf("The following items are currently NOT sold by %s:", storeChoice.getStoreName());
        System.out.printf("\n| item-Id | %-15s | Purchase-Category |  ", "Item-Name");
        System.out.println("\n-------------------------------------------------");
        List<InventoryItem> res = sdmInstance.getInventory().getListOfItemsNotSoldByStore(storeChoice);
        for (InventoryItem item: res){
            System.out.printf("|%-9d|%-17s|%-19s|\n",
                    item.getInventoryItemId(),
                    item.getItemName(),
                    item.getPurchaseCategory());
        }
    }

    private static void viewOrderHistory(SDM sdmInstance) {
        List<Order> history = sdmInstance.getOrderHistory().getOrders();
        System.out.println("Order history:");
        if (!history.isEmpty()) {

            System.out.println("==============================================================================================");
            for (Order order : history) {
                int orderId = order.getOrderId();
                Date orderDate = order.getOrderDate();
                String date = new SimpleDateFormat("dd/MM\tHH:mm").format(orderDate);
                //TODO: if static (only one store) show id and store name. if dynamic, do not show these details.

                boolean isStaticOrder = order.getStoresBoughtFrom().size() == 1;

                if (isStaticOrder) {
                    //static order
                    Iterator<Store> iterator = order.getStoresBoughtFrom().iterator();
                    int storeId = iterator.next().getStoreId();
                    String storeName = iterator.next().getStoreName();

                    System.out.printf("| Order-id: %d | %-10s | Store-Id: %-5d | %-13s |\n\n", orderId, date, storeId, storeName);
                }
                else {
                    //dynamic order
                    System.out.printf("| Order-id: %d | %-10s |\n\n", orderId, date);
                }

                float deliveryCost = order.getDeliveryCost();
                Cart cart = order.getCartForThisOrder();
                float cartTotal = cart.getCartTotalPrice();
                float total = cartTotal + deliveryCost;

                printCartDetailsForStaticOrder(cart);
                System.out.printf("\nTotal number of items: %d", order.getCartForThisOrder().getNumItemsInCart());
                System.out.printf("\nTotal types of items: %d", order.getCartForThisOrder().getNumberOfTypesOfItemsInCart());
                System.out.printf("\nTotal number of stores: %d", order.getNumberOfStoresInvolved());
                System.out.println("\nSubtotal: " + cartTotal);
                System.out.printf("Delivery fee: %.2f\n", deliveryCost);
                System.out.println("----------------");
                System.out.printf("Total: %.2f nis\n\n", total);
                System.out.println("==============================================================================================");
            }
        } else {
                System.out.println("No orders in history");
            }
        }


    private static void
    placeAnOrder(SDM sdmInstance) {

        Scanner in = new Scanner(System.in);
        int typeOfOrderInput;

        //1. Ask user if static or dynamic order
        System.out.println("Would you like a static order or dynamic order? Choose number of option");
        System.out.println("1. Static order");
        System.out.println("2. Dynamic order");

        typeOfOrderInput = in.nextInt();

        switch (typeOfOrderInput) {
            case 1: {
                placeAStaticOrder(sdmInstance);
                break;
            }
            case 2: {
                placeADynamicOrder(sdmInstance);
                break;
            }
            default:
                System.out.println("Error: Invalid input");
        }
    }

    public static void placeADynamicOrder (SDM sdmInstance) {

        Scanner in = new Scanner(System.in);
        String userInput;
        int chosenItemId;
        float amount;
        HashMap<InventoryItem, Float> mapItemsChosenToAmount = new HashMap<InventoryItem, Float>();

        //1. Ask user for date and time
        Date orderDate = getOrderDateFromUser();
        if (orderDate == null)
            return;

        //2. ask user for their location
        List<Integer> userLocation = getUserLocation(sdmInstance);
        if (userLocation.contains(-1))
            return;

        //3. Add items to cart
        while (true) {
            viewAllItemsInSystem(sdmInstance);
            System.out.println("\nTo add an item to your cart, enter its id number. To cancel order, enter 'Q'");
            System.out.println("To finish, enter 'finish'");
            userInput = in.nextLine().trim();

            if(userInput.equalsIgnoreCase("q")) {
                return;
            }

            if (userInput.equalsIgnoreCase("finish")) {
                break;
            }

            chosenItemId = Integer.parseInt(userInput);
            InventoryItem itemChosen = sdmInstance.getInventory().getInventoryItemById(chosenItemId);
            if (itemChosen == null) {
                System.out.println("Error: No such id found in inventory!");
            }

            amount = getAmount(itemChosen.getPurchaseCategory());
            mapItemsChosenToAmount.put(itemChosen,amount);
        }

        System.out.println("calculating the cheapest cart for you...");
        Cart cart = sdmInstance.findCheapestCartForUser(mapItemsChosenToAmount);
        Set<Store> storesBoughtFrom = cart.getStoresBoughtFrom();

//        String pattern = "dd/MM HH:mm";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//        String formattedDate = simpleDateFormat.format(orderDate);
        System.out.printf("Order Date: %1$td/%1$tm %1$tH:%1$tM\n", orderDate);
        System.out.println("My location: (" + userLocation.get(0) + ", " + userLocation.get(1) + ")");
        System.out.printf("Number Of Stores: %d\n", storesBoughtFrom.size());
        System.out.println("\nCart summary:");
        printCartDetailsForDynamicOrder(cart);
        float deliveryCost = calculateDeliveryCostForDynamicOrder(storesBoughtFrom, userLocation);
        System.out.printf("\nDelivery fee: %.2f", deliveryCost);
        System.out.println("\nCart subtotal:" + cart.getCartTotalPrice());
        System.out.print("----------------");
        float total = cart.getCartTotalPrice() + deliveryCost;
        System.out.printf("\nTotal: %.2f nis", total);
        System.out.println("");

        System.out.println("To confirm cart purchase, enter 'confirm'. To cancel order, enter 'Q'\"");
        userInput = in.nextLine().trim();

        switch (userInput.toLowerCase()) {
            case "confirm":
                if (!cart.getCart().isEmpty()) {
                    System.out.println("Order confirmed! (:");

                    Order order = new Order(userLocation, orderDate, deliveryCost, cart, storesBoughtFrom);
                    sdmInstance.addNewDynamicOrder(storesBoughtFrom, order);
                    return;
                }
                System.out.println("Cannot place an order for an empty cart!");
                break;

            case "q":
                break;
        }
    }

    private static float calculateDeliveryCostForDynamicOrder(Set<Store> storesBoughtFrom, List<Integer> userLocation) {

        float deliveryCostSum = 0, distance;
        List<Integer> storeLoc = new ArrayList<>();
        int ppk;

        for (Store store : storesBoughtFrom) {

            storeLoc = store.getStoreLocation();
            ppk = store.getDeliveryPpk();
            distance = getDistance(userLocation,storeLoc);
            deliveryCostSum += distance*ppk;
        }

        return deliveryCostSum;
    }

    public static void placeAStaticOrder(SDM sdmInstance) {

        Boolean isValidStore = false;

        List<Store> listOfStores = sdmInstance.getStores();
        List<Integer> listOfStoreIds = listOfStores.stream().map(i->i.getStoreId()).collect(Collectors.toList());

        Store storeChoice = null;
        List<Integer> userLocation = new ArrayList<>();
        int userInput;
        float amount;
        Cart cart = new Cart();
        Scanner in = new Scanner(System.in);
        String input;

        System.out.println("\nWhich store would you like to order from? Please enter the store-id from the following options. Enter 'Q' at anytime to cancel order: ");
        //1. Show stores and ask user for Store id
        while (!isValidStore) {
            printAvailableStores(sdmInstance);
            input = in.nextLine().trim();
            if (input.equalsIgnoreCase("q"))
                return;

            isValidStore = checkIfInputIsAllowed(input, listOfStoreIds);

            if (isValidStore)
                storeChoice = listOfStores.get(Integer.parseInt(input) - 1);
        }


//        storeChoice = listOfStores.get(userInput - 1);

        //TODO: See why sometimes still prints "Invalid Date and Time" after entering correct input
        //2. Ask user for date and time
        Date orderDate = getOrderDateFromUser();
        if (orderDate == null)
            return;

        //3. ask user for their location
        userLocation = getUserLocation(sdmInstance);
        if (userLocation.contains(-1))
            return;

        float distance = getDistance(userLocation, storeChoice.getStoreLocation());

        float deliveryCost = distance * storeChoice.getDeliveryPpk();

        //4. Choosing items to buy
        while (true) {
            System.out.println("\nTo add an item to your cart, enter 'add'. \nWhen finished, enter 'checkout'. \nTo cancel current order, enter 'cancel'");
            System.out.println("==========================================================================================================================");
            System.out.println("\nCart summary:");
            printCartDetailsForStaticOrder(cart);

//            System.out.println("Store: " + storeChoice.getStoreName());
//            //Explains how to format date: https://www.tutorialspoint.com/Date-Formatting-Using-printf
//            System.out.printf("Order Date: %1$td/%1$tm %1$tH:%1$tM\n", orderDate);
//            System.out.println("My location: (" + userLocation.get(0) + ", " + userLocation.get(1) + ")");
//            System.out.printf("PPK: %d\n", storeChoice.getDeliveryPpk());
//            System.out.printf("Distance from store: %.2f\n", distance);
//            System.out.println("\nCart summary:");
//            printCartDetailsForStaticOrder(cart);
//            System.out.printf("\nDelivery fee: %.2f", deliveryCost);
//            System.out.println("\nCart subtotal:" + cart.getCartTotalPrice());
//            System.out.print("----------------");
//            float total = cart.getCartTotalPrice() + deliveryCost;
//            System.out.printf("\nTotal: %.2f nis", total);
//
//            System.out.println("");

            input = in.nextLine().toLowerCase().trim();
            switch (input){
                case "checkout":
                    if (!cart.getCart().isEmpty()){
                        printCurrentOrderSummary(storeChoice, orderDate, userLocation, cart);
                        System.out.println("To confirm order, enter 'confirm'. To cancel order and go back to main menu, enter 'q':");
                        while (true){
                            input = in.nextLine().toLowerCase().trim();

                            switch (input){
                                case "confirm":
                                    System.out.println("Order confirmed! (:");

                                    Set<Store> storeOfThisOrder = new HashSet<Store>();
                                    storeOfThisOrder.add(storeChoice);
                                    Order order = new Order (userLocation, orderDate, deliveryCost, cart, storeOfThisOrder);
                                    sdmInstance.addNewStaticOrder(storeChoice, order);
                                    return;

                                case "q":
                                    System.out.println("Order cancelled ):");
                                    return;
                                default:
                                    System.out.println("Invalid command. Enter 'confirm' to send order or 'q' to cancel order");
                            }



                        }

                    }
                    System.out.println("Cannot place an order for an empty cart!");
                    break;

                case "add":
                    //ask user to enter ID for item to purchase
                    Boolean isValidPriceId = false;

                    while (!isValidPriceId) {
                        System.out.println("Please enter the Id for the item you wish to purchase. Enter 'cancel' to go back to previous menu: ");
                        printPriceTableForStore(sdmInstance, storeChoice);
                        input = in.nextLine().trim();
                        if (input.equalsIgnoreCase("cancel"))
                            break;
                        isValidPriceId = checkIfValidPriceId(sdmInstance, storeChoice, input);
                    }
                    if (isValidPriceId){
                        int priceID = Integer.parseInt(input);

                        int price = storeChoice.getMapItemToPrices().get(priceID);

                        //ask user to enter amount
                        InventoryItem itemChosen = storeChoice.getInventoryItemById(priceID);

                        amount = getAmount(itemChosen.getPurchaseCategory());
                        CartItem cartItem = new CartItem(itemChosen, amount, price, storeChoice);
                        cart.add(cartItem);
                    }
                    break;

                case "cancel":
                    return;

                default:
                    System.out.println("Invalid input! ):");
            }
        }
    }

    private static void printCurrentOrderSummary(Store storeChoice, Date orderDate, List<Integer> userLocation, Cart cart) {
        //float distance = getDistance(userLocation, storeChoice.getStoreLocation());
        float distance = storeChoice.getDeliveryCost(userLocation);
        float deliveryCost = distance * storeChoice.getDeliveryPpk();

        System.out.println("Store: " + storeChoice.getStoreName());
        //Explains how to format date: https://www.tutorialspoint.com/Date-Formatting-Using-printf
        System.out.printf("Order Date: %1$td/%1$tm %1$tH:%1$tM\n", orderDate);
        System.out.println("My location: (" + userLocation.get(0) + ", " + userLocation.get(1) + ")");
        System.out.printf("PPK: %d\n", storeChoice.getDeliveryPpk());
        System.out.printf("Distance from store: %.2f\n", distance);

        System.out.printf("\nDelivery fee: %.2f", deliveryCost);
        System.out.println("\nCart subtotal:" + cart.getCartTotalPrice());
        System.out.print("----------------");
        float total = cart.getCartTotalPrice() + deliveryCost;
        System.out.printf("\nTotal: %.2f nis", total);

        System.out.println("");
    }

    private static Date getOrderDateFromUser() {
        Date date = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM-kk:mm");
        dateTimeFormat.setLenient(false);
        Scanner in = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("Please enter date and time for delivery as dd/MM-hh:mm");

                //TODO: Check how to get date while ignoring white spaces
                String input = in.nextLine();
                if (input.equalsIgnoreCase("cancel"))
                    return null;

                date = dateTimeFormat.parse(input);

                if (date != null)
                    return date;

            } catch (ParseException e) {
//                System.out.println("Date and time are not in the correct pattern");
                //TODO: (Maybe) explain exactly what is wrong with date/time input
                System.out.println("Error: Invalid Date and time");
            }
        }
    }

    public static float getDistance(List<Integer> userLocation, List<Integer> storeLocation) {
        if (userLocation.size() != 2 || storeLocation.size() != 2){
            System.out.println("Error: Input lists must each contain 2 points!");
            return -1;
        }
        int xDelta = userLocation.get(0) -storeLocation.get(0);
        int yDelta = userLocation.get(1) -storeLocation.get(1);
        return (float) Math.sqrt((xDelta*xDelta)+(yDelta*yDelta));
    }

    //https://kodejava.org/how-do-i-align-string-print-out-in-left-right-center-alignment/
    //https://howtodoinjava.com/java/string/left-right-or-center-align-string/
    //https://www.java67.com/2014/06/how-to-format-float-or-double-number-java-example.html#:~:text=format(%22%25.,float%20data%20type%20in%20Java.
    //https://www.homeandlearn.co.uk/java/java_formatted_strings.html
    //https://stackoverflow.com/questions/15961130/align-printf-output-in-java


    private static void printCartDetailsForStaticOrder(Cart cart) {
//        System.out.println("Just entered printCartDetails");
//        System.out.println("Is myCart empty?: " + (myCart==null));

        if (!cart.isEmpty()) {
            System.out.printf("| Item-Id | %-14s | Unit price | %-19s | %-15s |\n", "Item-Name", "Amount", "Cost");
            System.out.println("---------------------------------------------------------------------------------");
            cart.getCart().forEach((k,v)->{
                String name = v.getItemName();
                ePurchaseCategory pCat = v.getPurchaseCategory();
                int price = v.getPrice();
                float amount = v.getItemAmount();
                float itemTotalCost = price*amount;
                System.out.printf("| %-7d | %-15s| %-6d nis | %-10s %-9.2f| %-11.2f nis |\n", k, name, price, pCat, amount, itemTotalCost);
            });
        }
        else {
            System.out.println("Your cart is empty");
        }
    }

    private static void printCartDetailsForDynamicOrder(Cart bestPriceCart) {

        if (!bestPriceCart.isEmpty()) {
            System.out.printf("| Item-Id | %-14s | Purchase-Category | %-19s | %-12s | Total Cost | Store-id |\n", "Item-Name", "Amount", "Best unit price");
            System.out.println("---------------------------------------------------------------------------------");
            bestPriceCart.getCart().forEach((k,v)->{
                String name = v.getItemName();
                ePurchaseCategory pCat = v.getPurchaseCategory();
                float amount = v.getItemAmount();
                int price = v.getPrice();
                float itemTotalCost = price*amount;
                int storeId = v.getStoreBoughtFrom().getStoreId();
                System.out.printf("| %-7d | %-15s | %-20s | %-9.2f | %-11d nis | %-11.2f nis| %-7d |\n", k, name, pCat, amount, price, itemTotalCost, storeId);
            });
        }
        else {
            System.out.println("Your cart is empty");
        }
    }



//    public Date validateDateFormat(String dateToValdate, String formatToValidate) {
//
//        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
//        //To make strict date format validation
//        formatter.setLenient(false);
//        Date parsedDate = null;
//        try {
//            parsedDate = formatter.parse(dateToValdate);
//            System.out.println("++validated DATE TIME ++" + formatter.format(parsedDate));
//
//        } catch (ParseException e) {
//            //Handle exception
//        }
//        return parsedDate;
//    }

    //If listOfAllowedInts==null, then the function just checks that input string can be parsed as Int
    private static Boolean checkIfInputIsAllowed(String input, List<Integer> listOfAllowedInts){
        try{
            int inputAsInt = Integer.parseInt(input);

            if (listOfAllowedInts != null){
                if (!listOfAllowedInts.contains(inputAsInt)){
                    return false;
                }
            }
            return true;

        } catch(NumberFormatException nfe){
            System.out.printf("Error: %s could not be parsed as Int!\n", input);
            return false;
        }
    }

    private static Boolean checkIfValidPriceId(SDM sdm, Store storeChoice, String input) {
        List<InventoryItem> fullInventory = sdm.getInventory().getListInventoryItems();
        List<InventoryItem> storeInventory = storeChoice.getInventoryItems();

        List<Integer> existingItems = fullInventory.stream().map(i-> i.getInventoryItemId()).collect(Collectors.toList());
        List<Integer> storeItemIds = storeInventory.stream().map(i-> i.getInventoryItemId()).collect(Collectors.toList());
        Boolean isValid = true;

        try{
            int inputAsInt = Integer.parseInt(input);

            if (!existingItems.contains(inputAsInt)){
                System.out.println("Invalid input: No reference found for itemId=" + inputAsInt);
                isValid = false;
            }
            if (!storeItemIds.contains(inputAsInt)){
                System.out.printf("Invalid input: Item %d (%s) is not currently sold at store %s\n",
                        inputAsInt, sdm.getInventory().getInventoryItemById(inputAsInt).getItemName(), storeChoice.getStoreName());
                isValid = false;
            }
            return isValid;
        } catch (NumberFormatException nfe){
            System.out.println("Invalid input!");
            return false;
        }
    }


    private static void printPriceTableForStore(SDM sdm, Store storeChoice) {
        List<InventoryItem> fullInventory = sdm.getInventory().getListInventoryItems();
        List<InventoryItem> storeInventory = storeChoice.getInventoryItems();

        String prompt = "";
        prompt = prompt.concat(String.format("\n|%s| %-15s |%s| %-13s |"," Item-Id ", "Item-Name"," Purchase-Category ", "Price"));
        prompt = prompt.concat("\n----------------------------------------------------------------");


        //TODO: maybe change structure of Store Inventory to hold CartItems instead?
        //Create string listing items and their details
        for (InventoryItem item : fullInventory) {
            boolean isSoldAtStore = storeInventory.contains(item);

            String s2 = String.format("\n| %-7d | %-15s | %-18s| %-13s |",
                    item.getInventoryItemId(),
                    item.getItemName(),
                    item.getPurchaseCategory(),
                    isSoldAtStore? storeChoice.getMapItemToPrices().get(item.getInventoryItemId()).toString() + " nis" : "not available"
            );

            prompt = prompt.concat(s2);
        }

        System.out.println(prompt);
        System.out.println("");

    }

    private static void printAvailableStores(SDM sdm){
        System.out.printf("| %-8s | %-22s | %-5s |\n", "Store-id", "Name", "PPK");
        System.out.println("---------------------------------------------");
        for (Store store : sdm.getStores()) {
            System.out.printf("| %-8d | %-22s | %-5d |\n", store.getStoreId(), store.getStoreName(), store.getDeliveryPpk());
        }
    }


    private static List<Integer> getUserLocation(SDM sdmInstance) {
        List<SDMStore> listOfStores = sdmInstance.getListOfSDMStores();
        List<List<Integer>> listOfStoreLocations = sdmInstance.getListOfStoreLocations(listOfStores);

        boolean legalRange, spotNotTaken, legalSize;
        Scanner src = new Scanner(System.in);
        src.useDelimiter(",");

        while (true) {
            try{
                legalSize = true;
                legalRange = true;
                spotNotTaken = true;

                //https://stackoverflow.com/questions/27599847/convert-comma-separated-string-to-list-without-intermediate-container
                System.out.println("What is your current location? (Please enter comma-separated whole numbers between [1,50])");
                String input = src.nextLine().trim();

                if (input.equalsIgnoreCase("cancel")) {
                    return Collections.singletonList(-1);
                }

                List<Integer> list = Stream.of(input.split(",")).map(Integer::parseInt).collect(Collectors.toList());
                if (list.size() > 2){
                    legalSize = false;
                    System.out.println("Invalid input: too many points entered!");
                }

                if (list.get(0) < 1 || list.get(0) > 50 || list.get(1) < 1 || list.get(1) > 50) {
                    legalRange = false;
                    System.out.println("Invalid input: You entered " + "(" + list.get(0) + ", " + list.get(1) + "), but Coordinates must be in range [1,50]");
                }

                if (listOfStoreLocations.contains(list)){
                    spotNotTaken = false;
                    System.out.println("Invalid input: The location (" + list.get(0) + ", " + list.get(1) + ") is already occupied by a store!");
                }
                if (legalRange && legalSize && spotNotTaken){
                    System.out.println("Great location!");
                    return list;
                }

            } catch (NumberFormatException nfe){
                System.out.println("Number format exception: " + nfe.getMessage());
            } catch (NoSuchElementException nse){
                System.out.println("No Such element exception: " + nse.getMessage());
            } catch (IllegalStateException ise){
                System.out.println("Illegal state exception: " + ise.getMessage());
            } catch (IndexOutOfBoundsException obe){
                System.out.println("Input must be two comma-separated whole numbers between 0 and 50!");
            }
        }
    }


    private static boolean checkUserWantsToCancelOrder() {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("Are you sure you want to cancel current order? (Y/N)");
            //System.out.println("Cancel operation? (Y/N)");
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("y"))
                return true;

            if (input.equalsIgnoreCase("n"))
                return false;
        }
    }

    private static float getAmount(ePurchaseCategory purchaseCat) {
        //boolean isValidQuantity = false;
        float res;
        Scanner in = new Scanner(System.in);
        DecimalFormat df;

        while (true) {
            try {
                if (purchaseCat == ePurchaseCategory.WEIGHT) {
                    df = new DecimalFormat("0.00");
                    System.out.println("Please enter order weight in kgs: ");
                    res = in.nextFloat();
                    if (res > 0)
                        return Float.valueOf(df.format(res));
                } else if (purchaseCat == ePurchaseCategory.QUANTITY) {
                    System.out.println("Please enter order quantity: ");
                    res = in.nextFloat();

                    //https://stackoverflow.com/questions/9898512/how-to-test-if-a-double-is-an-integer
                    if (res == Math.round(res) && res > 0) {
                        return Math.round(res);
                    }
                    System.out.println("Invalid input: Quantity must be a positive, whole number");
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input!");
                in.nextLine().trim();
            }
        }
    }

    private static void viewAllStoresInSystem(SDM sdm) {
        List<Store> stores = sdm.getStores();
        for (Store store: stores){
            System.out.println("\n===========================================================================\n");
            System.out.printf("| Store-id: %-3d | Store-name: %-10s |\n",store.getStoreId(), store.getStoreName());
            viewInventoryForStore(store);
            viewOrdersForStore(store);
            System.out.printf("\nPPK: %d", store.getDeliveryPpk());
            System.out.printf("\nTotal income from deliveries: %.2f\n", store.getTotalDeliveryIncome());
        }
        System.out.println("\n===========================================================================\n");
    }

    private static void viewOrdersForStore(Store store) {
        List<Order> orders = store.getOrders();
        //System.out.println("\nOrders: ");
        if (orders.isEmpty()){
            System.out.println("\nNo orders yet for store " + store.getStoreId());
        } else{
            System.out.printf("\nOrder history for store %d:\n", store.getStoreId());
            System.out.println("| Order Date  | Tot. num. items in cart | Cart subtotal | Delivery cost | Total |");
            System.out.println("---------------------------------------------------------------------------------");
            store.getOrders().stream().forEach(o -> viewOrderDetails(o));
            System.out.println("");
        }
    }

    private static void viewOrderDetails(Order o) {
        String date = new SimpleDateFormat("dd/MM\thh:mm").format(o.getOrderDate());

        System.out.printf("| %-10s | %-23d | %-13.2f | %-13.2f | %-5.2f |\n" , date, o.getCartForThisOrder().getNumItemsInCart(), o.getCartTotal(), o.getDeliveryCost(), o.getCartTotal()+o.getDeliveryCost());

    }

    private static void viewAllItemsInSystem(SDM sdmInstance) {

        System.out.println("All items in system:");
        Inventory inventory = sdmInstance.getInventory();
        System.out.printf("\n| item-Id | %-15s | Purchase-Category | Amount of stores carrying item |   Ave price   | Total amount sold | ", "Item-Name");
        System.out.println("\n----------------------------------------------------------------------------------------------------------------------");

        for (InventoryItem item: inventory.getListInventoryItems()){
            int numStoresCaryingItem = inventory.getMapItemsToStoresWithItem().get(item).size();
            float avePrice = inventory.getMapItemsToAvePrice().get(item);
            float totalAmountSold = inventory.getMapItemsToTotalSold().get(item);

            String s = String.format("%.2f", totalAmountSold);
            if (item.getPurchaseCategory() == ePurchaseCategory.WEIGHT)
                s = s.concat( " kgs");
            else if (item.getPurchaseCategory() == ePurchaseCategory.QUANTITY)
                s = s.concat( " pckgs");

            System.out.printf("|%-9d|%-17s|%-19s|%-32d|%-10.2f nis |%-18s |\n",
                    item.getInventoryItemId(),
                    item.getItemName(),
                    item.getPurchaseCategory(),
                    numStoresCaryingItem,
                    avePrice,
                    s);
        }
    }

    private static void viewInventoryForStore(Store store) {
        List<InventoryItem> storeInventory = store.getInventoryItems();
        System.out.println("\nCurrent Inventory: ");
        System.out.printf("| item-Id | %-15s | Purchase-Category |   price   | amount sold |", "item-Name");
        System.out.println("\n-----------------------------------------------------------------------");
        storeInventory.forEach(item->{
            int id = item.getInventoryItemId();
            int price = store.getMapItemToPrices().get(id);
            float amountSold = store.getMapItemsToAmountSold().get(id);
            String s = String.format("%.2f", amountSold);
            if (item.getPurchaseCategory() == ePurchaseCategory.WEIGHT)
                s = s.concat( " kgs");
            else if (item.getPurchaseCategory() == ePurchaseCategory.QUANTITY)
                s = s.concat( " pckgs");


            System.out.printf("| %-7d | %-15s | %-18s| %-5d nis | %-11s |\n",id,item.getItemName(), item.getPurchaseCategory(),
                    price, s);

        });
    }


    private static void showPossibleCommands(boolean fileSuccessfullyLoaded) {
        //System.out.println("Press '1' to load a new file, or 'Q' to quit at any time");
        System.out.println("\nPlease select an option from the following menu. Enter 'Q' to quit:");
        System.out.println("==============================================================================================");
        System.out.println("1) Load a new SDM file");

        if (fileSuccessfullyLoaded) {
            //System.out.println("1)Create a new student");
            System.out.println("2) Display store details");
            System.out.println("3) View all items in system");
            System.out.println("4) Place an order");
            System.out.println("5) View Order History");
            System.out.println("6) Update inventory for store");
        }
        System.out.println("Q) Quit");
    }

    public static String readFilePath() {
        boolean isValidPath = false;
        String s;
        File f;
        Scanner in = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("Enter file path. To go back to main menu, enter 'cancel': ");
                String fileName = in.nextLine().trim();

                if (fileName.equalsIgnoreCase("cancel"))
                    return "q";

                else if (fileName.length() <= 3 || !fileName.substring(fileName.length() - 3).toLowerCase().equals("xml")) {
                    System.out.println("Error: Invalid Path. Please only enter path to existing XML file\n");
                } else {
                    f = new File(fileName);
                    if (f.exists())
                        return fileName;
                    System.out.println("Could not load file at location: " + fileName);
                }

            } catch (NoSuchElementException | IllegalStateException e) {
                System.out.println("Cannot open: " + e.getMessage());
            } catch (SecurityException se) {
                System.out.println("Error: Security exception - " + se.getMessage());
            } catch (NullPointerException npe) {
                System.out.println("Null pointer exception: " + npe.getMessage());
            }
        }
    }


    private static void printDetailsOfSDM(SuperDuperMarketDescriptor sdm) {
        List<SDMItem> sdmItems = sdm.getSDMItems().getSDMItem();
        List<SDMStore> sdmStores = sdm.getSDMStores().getSDMStore();

        System.out.println("SDM-Items: [");
        for (SDMItem item : sdmItems) {
            System.out.println("id= " + item.getId() + ", name= " + item.getName() + ", purchase Category = " + item.getPurchaseCategory() + ",");
        }

        System.out.println("\nSDM-Stores: [");
        for (SDMStore store : sdmStores) {
            String loc = "[" + store.getLocation().getY() + ", " + store.getLocation().getY() + "]";
            String sells = "sells = [";
            for (SDMSell itemSold : store.getSDMPrices().getSDMSell()) {
                sells = sells.concat(" {item-id=" + itemSold.getItemId() + ", price=" + itemSold.getPrice() + "},");
            }
            sells = sells.concat("]");
            System.out.println("Store-Id=" + store.getId() + ", store-name= " + store.getName() + ", location= " + loc + ", " + sells);
        }
    }

    //    public Date validateDateFormat(String dateToValdate, String formatToValidate) {
//
//        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
//        //To make strict date format validation
//        formatter.setLenient(false);
//        Date parsedDate = null;
//        try {
//            parsedDate = formatter.parse(dateToValdate);
//            System.out.println("++validated DATE TIME ++" + formatter.format(parsedDate));
//
//        } catch (ParseException e) {
//            //Handle exception
//        }
//        return parsedDate;
//    }


    //    private static int getIntFromUser() {
//        Scanner in = new Scanner(System.in);
//        int userInput;
//        String userInputStr;
//        boolean comingFromCancel = false;
//
//        while (true) {
//            try {
//                comingFromCancel = false;
//                userInputStr = in.nextLine().trim();
//                if (userInputStr.equalsIgnoreCase("q")) {
//                    if (checkUserWantsToCancelOrder()) {
//                        return -1;
//                    }
//                    comingFromCancel = true;
//                }
//                userInput = Integer.parseInt(userInputStr);
//                break;
//            } catch (InputMismatchException e) {
//                System.out.println("Invalid input!");
//            } catch (NumberFormatException nfe) {
//                if (!comingFromCancel) {
//                    System.out.println("Invalid input: Please only enter whole numbers, or 'Q' to quit");
//                    comingFromCancel = false;
//                }
//            }
//        }
//        return userInput;
//    }


    //    private static int getPriceIdFromUser(SDM sdm, Store storeChoice) {
//        List<InventoryItem> fullInventory = sdm.getInventory().getListInventoryItems();
//        List<InventoryItem> storeInventory = storeChoice.getInventoryItems();
//
//
//        List<Integer> existingItems = fullInventory.stream().map(i-> i.getInventoryItemId()).collect(Collectors.toList());
//        List<Integer> storeItemIds = storeInventory.stream().map(i-> i.getInventoryItemId()).collect(Collectors.toList());
//
//        System.out.println("Please enter the Id for the item you wish to purchase: ");
//
//        while (true) {
//            printPriceTableForStore(sdm,storeChoice);
//
//            int priceId = getIntFromUser();
//            //int priceId = getIntFromUser(prompt);
//
//            if (priceId == -1)
//                return -1;
//
//            if (!existingItems.contains(priceId)) {
//                System.out.println("aaaaaaaa");
//                System.out.println("Invalid input: No reference found for itemId=" + priceId);
//            } else if (existingItems.contains(priceId) && !storeItemIds.contains(priceId)) {
//                System.out.println("The item you selected is not currently available at this store. ");
//            } else
//                return priceId;
//        }
//    }

    //    private static int getStoreIdFromUser(SDM sdmInstance) {
//        List<Store> listOfStores = sdmInstance.getStores();
//        List<Integer> storeIds = listOfStores.stream().map(i->i.getStoreId()).collect(Collectors.toList());
//
//        Scanner in = new Scanner(System.in);
//        boolean isValidStoreId = false;
//
//        int userInput;
//
//        while (true) {
//            try {
//
//                printAvailableStores(sdmInstance);
//                userInput = getIntFromUser();
//                if (userInput == -1)
//                    return -1;
//
//                if (!storeIds.contains(userInput))
//                    System.out.println("Invalid Input: could not find existing store with id " + userInput);
//
//                else if (storeIds.contains(userInput))
//                    return userInput;
//
//            } catch (InputMismatchException e) {
//                System.out.println("Invalid input!");
//            } catch (NumberFormatException nfe) {
//                System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
//                //System.out.println(prompt);
//            }
//        }
//    }

}
