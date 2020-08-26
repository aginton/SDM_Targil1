package course.java.sdm.console;

import course.java.sdm.engine.Inventory.Inventory;
import course.java.sdm.engine.Inventory.InventoryItem;
import course.java.sdm.engine.Inventory.ePurchaseCategory;
import course.java.sdm.engine.Orders.Cart;
import course.java.sdm.engine.Orders.CartItem;
import course.java.sdm.engine.Orders.Order;
import course.java.sdm.engine.Orders.eOrderType;
import course.java.sdm.engine.SDM.SDM;
import course.java.sdm.engine.Store.Store;
//import course.java.sdm.engine.jaxb.schema.generated.SDMItem;
//import course.java.sdm.engine.jaxb.schema.generated.SDMSell;
//import course.java.sdm.engine.jaxb.schema.generated.*;
import course.java.sdm.engine.jaxb.schema.generated.SDMStore;
//import course.java.sdm.engine.jaxb.schema.generated.SDMStores;
//import course.java.sdm.engine.jaxb.schema.generated.SuperDuperMarketDescriptor;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UIMain {

    public static void main(String[] args) {
        boolean isSDMLoaded = false;
        boolean wantsToQuit = false;
        SDM sdmInstance = new SDM();

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
                    if (isSDMLoaded) {
                        viewAllItemsInSystem(sdmInstance);
                    }
                    break;

                //add an order
                case "4":
                    if (isSDMLoaded) {
                        placeAnOrder(sdmInstance);
                    }

                    break;

                    //view order history
                case "5":
                    if (isSDMLoaded) {
                        viewOrderHistory(sdmInstance);
                    }
                    break;

                    //update store inventory
                case "6":
                    if (isSDMLoaded) {
                        updateInventoryForStore(sdmInstance);
                    }
                    break;

                case "q":
                    wantsToQuit = true;
                    break;

                default:
                    System.out.println("Invalid input. " + operator + " does not correspond to any command!");
            }
        }

        System.out.println("Goodbye!");

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
        List<Integer> listOfStoreIds = listOfStores.stream().map(i -> i.getStoreId()).collect(Collectors.toList());

        //1. Show list of stores and ask user for id of store to update
        while (!isValidStoreChoice) {
            System.out.println("\nWhich store inventory do you wish to update? \nPlease enter store-id from the following list, or enter 'cancel' to go back to main menu:");
            printAvailableStores(sdmInstance);
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("cancel"))
                return;

            isValidStoreChoice = checkIfInputIsAllowed(input, listOfStoreIds);

            if (isValidStoreChoice)
                storeChoice = listOfStores.get(Integer.parseInt(input) - 1);
        }

        //2. Ask user if they want to add new item, delete item, or update price for existing item for storeChoice
        while (true) {
            System.out.println("\nStore: " + storeChoice.getStoreName());
            System.out.println("To add an existing item to this stores inventory, enter 'add'. ");
            System.out.println("To update price of item in this store's inventory, enter 'update'");
            System.out.println("To remove an item from store's inventory, enter 'remove'");
            System.out.println("When finished, enter 'done'");
            System.out.println("=======================================================================");

            String input = in.nextLine().trim().toLowerCase();
            switch (input) {
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
    }




    private static void updatePricesForStore(SDM sdm, Store storeChoice) {
        if (storeChoice.getInventoryItems().size() == 0){
            System.out.printf("Store %s's inventory is currently empty ):");
            return;
        }
        Scanner in = new Scanner(System.in);
        System.out.printf("Which item's price do you wish to update? Enter Item-ID from the following list, or enter 'cancel' to go back to previous menu:\n", storeChoice.getStoreName());
        printItemsSoldByStore(sdm, storeChoice);
        String userInput = in.nextLine().trim();

        if (checkIfInputIsAllowed(userInput, null)){
            InventoryItem chosenItem = storeChoice.getInventoryItemById(Integer.parseInt(userInput));
            if ( chosenItem!= null){

                while (true) {
                    System.out.printf("Enter price for %s at store %s. Please enter a positive integer, or enter 'cancel' to go back to previous menu:\n", chosenItem.getItemName(), storeChoice.getStoreName());
                    String input = in.nextLine().trim();

                    if (input.equalsIgnoreCase("cancel"))
                        return;

                    if (checkIfInputIsAllowed(input, null)) {
                        int price = Integer.parseInt(input);
                        if (price > 0) {
                            sdm.updateItemPriceAtStore(chosenItem, storeChoice, price);
                            System.out.printf("\nPrice of item %s at store %s was successfully changed to %d", chosenItem.getItemName(), storeChoice.getStoreName(), price);
                            return;
                        } else
                            System.out.println("Error: Price must be a positive, whole number!");
                    }
                }
            }
        }
        System.out.printf("Invalid Input: %s has no item with id = %s\n", storeChoice.getStoreName(), userInput);
    }


    private static void removeItemFromStoreInventory(SDM sdm, Store storeChoice) {
        if (storeChoice.getInventoryItems().size() == 0){
            System.out.printf("Store %s's inventory is currently empty ):");
            return;
        }
        Scanner in = new Scanner(System.in);
        System.out.printf("Enter ID of item that is to be BANISHED by %s! Please select from the following list, or enter 'cancel' to go back to previous menu:\n", storeChoice.getStoreName());
        printItemsSoldByStore(sdm, storeChoice);

        String output = sdm.removeItemFromStore(in.nextLine().trim(), storeChoice);
        System.out.println(output);
    }

    private static void addExistingItemToStoreInventory(SDM sdm, Store storeChoice) {
        if (sdm.getInventory().getListOfItemsNotSoldByStore(storeChoice).size() == 0){
            System.out.printf("No existing items can currently be added to store's inventory! %s already has everything!\n", storeChoice.getStoreName());
            return;
        }

        Scanner in = new Scanner(System.in);
        System.out.printf("Enter Item-ID for item you wish to add to %s's inventory. Please select from the following list, or enter 'cancel' to go back to previous menu:\n", storeChoice.getStoreName());
        printItemsNotSoldByStore(sdm, storeChoice);
        String userInput = in.nextLine().trim();
        String output = sdm.canAddExistingItemToStore(userInput, storeChoice);

        if (!output.isEmpty()){
            System.out.println(output);
            return;
        }
        InventoryItem chosenItem = sdm.getInventory().getInventoryItemById(Integer.parseInt(userInput));
        int price = 0;
        Boolean isValidPrice = false;

        while (!isValidPrice) {
            System.out.printf("Enter price for %s at store %s. Please enter a positive integer, or enter 'cancel' to go back to previous menu:\n", chosenItem.getItemName(), storeChoice.getStoreName());
            String input = in.nextLine().trim();

            if (input.equalsIgnoreCase("cancel"))
                return;

            if (checkIfInputIsAllowed(input, null)) {
                price = Integer.parseInt(input);
                if (price > 0) {
                    isValidPrice = true;
                } else
                    System.out.println("Error: Price must be a positive, whole number!");
            }
        }

        System.out.printf("Adding item %s (id=%d) to %s's inventory, at price=%d\n",
                chosenItem.getItemName(), chosenItem.getInventoryItemId(), storeChoice.getStoreName(), price);

        sdm.addInventoryItemToStore(chosenItem, storeChoice, price);
    }


    private static void printItemsNotSoldByStore(SDM sdmInstance, Store storeChoice) {
        System.out.printf("\n| item-Id | %-15s | Purchase-Category |  ", "Item-Name");
        System.out.println("\n-------------------------------------------------");
        List<InventoryItem> res = sdmInstance.getInventory().getListOfItemsNotSoldByStore(storeChoice);
        for (InventoryItem item : res) {
            System.out.printf("|%-9d|%-17s|%-19s|\n",
                    item.getInventoryItemId(),
                    item.getItemName(),
                    item.getPurchaseCategory());
        }
    }

    private static void printItemsSoldByStore(SDM sdmInstance, Store storeChoice) {
        System.out.printf("\n| item-Id | %-15s | Purchase-Category | Current Price |", "Item-Name");
        System.out.println("\n-----------------------------------------------------------------");
        List<InventoryItem> res = storeChoice.getInventoryItems();
        for (InventoryItem item : res) {
            System.out.printf("|%-9d|%-17s|%-19s| %-13d |\n",
                    item.getInventoryItemId(),
                    item.getItemName(),
                    item.getPurchaseCategory(),
                    storeChoice.getMapItemToPrices().get(item.getInventoryItemId()));
        }
    }



    private static void viewOrderHistory(SDM sdmInstance) {
        List<Order> history = sdmInstance.getOrderHistory().getOrders();
        System.out.println("Order history:");
        if (!history.isEmpty()) {

            System.out.println("==============================================================================================");
            for (Order order : history) {
                int orderId = order.getOrderId().getId();
                Date orderDate = order.getOrderDate();
                String date = new SimpleDateFormat("dd/MM\tHH:mm").format(orderDate);

                if (order.getOrderType() == eOrderType.STATIC_ORDER) {
                    Iterator<Store> iterator = order.getStoresBoughtFrom().iterator();
                    Store storeForOrder = iterator.next();
                    int storeId = storeForOrder.getStoreId();
                    String storeName = storeForOrder.getStoreName();
                    System.out.printf("| Order-id: %d | %-10s | Store-Id: %-5d | %-13s |\n\n", orderId, date, storeId, storeName);
                } else {
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
        int typeOfOrderInput = 0;
        boolean isValidInput = false;


        while (!isValidInput){
            //1. Ask user if static or dynamic order
            System.out.println("\nWould you like a static order or dynamic order? Choose number of option");
            System.out.println("1. Static order");
            System.out.println("2. Dynamic order");

            String input = in.nextLine().trim();
            if (!checkIfInputIsAllowed(input, null)){
                System.out.printf("Error: %s is not a valid integer\n", input);
            }
            else{
                typeOfOrderInput = Integer.parseInt(input);
                if (typeOfOrderInput == 1 || typeOfOrderInput == 2)
                    isValidInput = true;
                else
                    System.out.printf("Error: %d does not correspond to available order type\n", typeOfOrderInput);
            }
        }

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

    public static void placeADynamicOrder(SDM sdmInstance) {

        Scanner in = new Scanner(System.in);
        String userInput;
        int chosenItemId;
        float amount;
        boolean isValidItemId;
        HashMap<InventoryItem, Float> mapItemsChosenToAmount = new HashMap<InventoryItem, Float>();
        List<Integer> existingInventoryItemIds = sdmInstance.getInventory().getListOfInventoryItemIds();

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
            System.out.println("\nTo add an item to your cart, enter 'add'. To cancel order, enter 'q'");
            System.out.println("When finished, enter 'checkout'");

            userInput = in.nextLine().toLowerCase().trim();
            switch (userInput) {
                case "add":

                    viewAllItemsInSystem(sdmInstance);
                    System.out.println("Please enter the Id for the item you wish to purchase.");
                    userInput = in.nextLine().trim();

                    isValidItemId = checkIfInputIsAllowed(userInput, existingInventoryItemIds);
                    if (!isValidItemId) {
                        System.out.printf("Error: No item with id=%s can be found in system!\n", userInput);
                    } else {
                        chosenItemId = Integer.parseInt(userInput);
                        InventoryItem itemChosen = sdmInstance.getInventory().getInventoryItemById(chosenItemId);
                        amount = getAmount(itemChosen.getPurchaseCategory());
                        mapItemsChosenToAmount.put(itemChosen, amount);
                    }
                    break;

                case "checkout":
                    System.out.println("calculating the cheapest cart for you...\n");
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Cart cart = sdmInstance.findCheapestCartForUser(mapItemsChosenToAmount);
                    Set<Store> storesBoughtFrom = cart.getStoresBoughtFrom();

                    System.out.println("Your Order: ");
                    System.out.printf("Order Date: %1$td/%1$tm %1$tH:%1$tM\n", orderDate);
                    System.out.println("My location: (" + userLocation.get(0) + ", " + userLocation.get(1) + ")");
                    System.out.printf("Number Of Stores: %d\n", storesBoughtFrom.size());
                    System.out.println("\nCart summary:");
                    printCartDetailsForDynamicOrder(cart);
                    float deliveryCost = calculateDeliveryCostForDynamicOrder(storesBoughtFrom, userLocation);
                    System.out.printf("\nDelivery fee: %.2f", deliveryCost);
                    System.out.println("\nCart subtotal: " + cart.getCartTotalPrice());
                    System.out.print("----------------");
                    float total = cart.getCartTotalPrice() + deliveryCost;
                    System.out.printf("\nTotal: %.2f nis", total);
                    System.out.println("");

                    System.out.println("To confirm cart purchase, enter 'confirm'. To cancel order, enter 'q'\"");
                    userInput = in.nextLine().toLowerCase().trim();

                    switch (userInput) {
                        case "confirm":
                            if (!cart.getCart().isEmpty()) {
                                System.out.println("Order confirmed! (:");
                                System.out.println("Thank you for using our application.");

                                Order order = new Order(userLocation,
                                        orderDate,
                                        deliveryCost,
                                        cart,
                                        storesBoughtFrom,
                                        eOrderType.DYNAMIC_ORDER);
                                sdmInstance.addNewDynamicOrder(storesBoughtFrom, order);
                                return;
                            }
                            System.out.println("Cannot place an order for an empty cart!");
                            break;

                        case "q":
                            System.out.println("Order canceled");
                            return;
                        default:
                            System.out.println("Invalid input!");
                    }
                    break;

                case "q":
                    System.out.println("Order canceled");
                    return;
                default:
                    System.out.println("Invalid input!");
            }
        }
    }

    private static float calculateDeliveryCostForDynamicOrder(Set<Store> storesBoughtFrom, List<Integer> userLocation) {

        float deliveryCostSum = 0, distance;
        int ppk;

        for (Store store : storesBoughtFrom) {

            ppk = store.getDeliveryPpk();
            distance = store.getDistance(userLocation);
            deliveryCostSum += distance * ppk;
        }

        return deliveryCostSum;
    }

    public static void placeAStaticOrder(SDM sdmInstance) {

        Boolean isValidStore = false;

        List<Store> listOfStores = sdmInstance.getStores();
        List<Integer> listOfStoreIds = listOfStores.stream().map(i -> i.getStoreId()).collect(Collectors.toList());

        Store storeChoice = null;
        List<Integer> userLocation = new ArrayList<>();
        float amount;
        Cart cart = new Cart();
        Scanner in = new Scanner(System.in);
        String input;

        //1. Show stores and ask user for Store id
        while (!isValidStore) {
            System.out.println("\nWhich store would you like to order from? \nPlease enter store-id from the following list, or enter 'Q' to go back to main menu:");
            printAvailableStores(sdmInstance);
            input = in.nextLine().trim();
            if (input.equalsIgnoreCase("q"))
                return;

            isValidStore = checkIfInputIsAllowed(input, listOfStoreIds);

            if (isValidStore)
                storeChoice = listOfStores.get(Integer.parseInt(input) - 1);
            else
                System.out.printf("Invalid input: %s does not correspond to id of available stores\n", input);
        }

        //2. Ask user for date and time
        Date orderDate = getOrderDateFromUser();
        if (orderDate == null)
            return;

        //3. ask user for their location
        userLocation = getUserLocation(sdmInstance);
        if (userLocation.contains(-1))
            return;

        float distance = storeChoice.getDistance(userLocation);
        float deliveryCost = distance * storeChoice.getDeliveryPpk();

        //4. Choosing items to buy
        while (true) {
            System.out.println("\nTo add an item to your cart, enter 'add'. \nWhen finished, enter 'checkout'. \nTo cancel current order, enter 'Q'");
            System.out.println("==========================================================================================================================");
            System.out.println("\nCart summary:");
            printCartDetailsForStaticOrder(cart);

            input = in.nextLine().toLowerCase().trim();
            switch (input) {
                case "checkout":
                    if (!cart.getCart().isEmpty()) {
                        printCurrentOrderSummary(storeChoice, orderDate, userLocation, cart, distance, deliveryCost);
                        System.out.println("To confirm order, enter 'confirm'. To cancel order and go back to main menu, enter 'q':");
                        while (true) {
                            input = in.nextLine().toLowerCase().trim();

                            switch (input) {
                                case "confirm":
                                    System.out.println("Order confirmed! (:");
                                    System.out.println("Thank you for using our application.");

                                    Set<Store> storeOfThisOrder = new HashSet<Store>();
                                    storeOfThisOrder.add(storeChoice);
                                    Order order = new Order(userLocation,
                                            orderDate,
                                            deliveryCost,
                                            cart,
                                            storeOfThisOrder,
                                            eOrderType.STATIC_ORDER);
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
                        System.out.println("Please enter the Id for the item you wish to purchase. Enter 'Q' to go back to previous menu: ");
                        printPriceTableForStore(sdmInstance, storeChoice);
                        input = in.nextLine().trim();
                        if (input.equalsIgnoreCase("q"))
                            break;
                        isValidPriceId = checkIfValidPriceId(sdmInstance, storeChoice, input);
                    }
                    if (isValidPriceId) {
                        int priceID = Integer.parseInt(input);

                        int price = storeChoice.getMapItemToPrices().get(priceID);

                        //ask user to enter amount
                        InventoryItem itemChosen = storeChoice.getInventoryItemById(priceID);

                        amount = getAmount(itemChosen.getPurchaseCategory());
                        CartItem cartItem = new CartItem(itemChosen, amount, price, storeChoice);
                        cart.add(cartItem);
                    }
                    break;

                case "q":
                    return;

                default:
                    System.out.println("Invalid input! ):");
            }
        }
    }

    private static void printCurrentOrderSummary(Store storeChoice, Date orderDate, List<Integer> userLocation, Cart cart, float distance, float deliveryCost) {

        System.out.println("Store: " + storeChoice.getStoreName());
        System.out.printf("Order Date: %1$td/%1$tm %1$tH:%1$tM\n", orderDate);
        System.out.println("My location: (" + userLocation.get(0) + ", " + userLocation.get(1) + ")");
        System.out.printf("PPK: %d\n", storeChoice.getDeliveryPpk());
        System.out.printf("Distance from store: %.2f\n", distance);
        System.out.printf("\nDelivery fee: %.2f", deliveryCost);
        System.out.println("\nCart subtotal:" + cart.getCartTotalPrice());
        System.out.print("----------------");
        float total = cart.getCartTotalPrice() + deliveryCost;
        System.out.printf("\nTotal: %.2f nis\n", total);
    }

    private static Date getOrderDateFromUser() {
        Date date = new Date();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM-kk:mm");
        dateTimeFormat.setLenient(false);
        Scanner in = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("Please enter date and time for delivery as dd/MM-hh:mm (day/month-hour:min).");
                System.out.println("Enter 'Q' to go back to main menu.");

                String input = in.nextLine();
                if (input.equalsIgnoreCase("q"))
                    return null;

                date = dateTimeFormat.parse(input);

                if (date != null)
                    return date;

            } catch (ParseException e) {
                System.out.println("Error: Invalid Date and time");
            }
        }
    }

    private static void printCartDetailsForStaticOrder(Cart cart) {
        if (!cart.isEmpty()) {
            System.out.printf("| Item-Id | %-14s | Unit price | %-19s | %-15s |\n", "Item-Name", "Amount", "Cost");
            System.out.println("---------------------------------------------------------------------------------");
            cart.getCart().forEach((k, v) -> {
                String name = v.getItemName();
                ePurchaseCategory pCat = v.getPurchaseCategory();
                int price = v.getPrice();
                float amount = v.getItemAmount();
                float itemTotalCost = price * amount;
                System.out.printf("| %-7d | %-15s| %-6d nis | %-10s %-9.2f| %-11.2f nis |\n", k, name, price, pCat, amount, itemTotalCost);
            });
        } else {
            System.out.println("Your cart is empty");
        }
    }

    private static void printCartDetailsForDynamicOrder(Cart bestPriceCart) {

        if (!bestPriceCart.isEmpty()) {
            System.out.printf("| Item-Id | %-14s | Purchase-Category | %-16s | %-12s | %-14s | Store-id |\n", "Item-Name", "Amount", "Best unit price", "Total Cost");
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
            bestPriceCart.getCart().forEach((k, v) -> {
                String name = v.getItemName();
                ePurchaseCategory pCat = v.getPurchaseCategory();
                float amount = v.getItemAmount();
                int price = v.getPrice();
                float itemTotalCost = price * amount;
                int storeId = v.getStoreBoughtFrom().getStoreId();
                System.out.printf("| %-7d | %-15s | %-16s | %-16.2f | %-11d nis | %-11.2f nis| %-8d |\n", k, name, pCat, amount, price, itemTotalCost, storeId);
            });
        } else {
            System.out.println("Your cart is empty");
        }
    }

    private static Boolean checkIfInputIsAllowed(String input, List<Integer> listOfAllowedInts) {
        try {
            int inputAsInt = Integer.parseInt(input);

            if (listOfAllowedInts != null) {
                if (!listOfAllowedInts.contains(inputAsInt)) {
                    return false;
                }
            }
            return true;

        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private static Boolean checkIfValidPriceId(SDM sdm, Store storeChoice, String input) {
        List<InventoryItem> fullInventory = sdm.getInventory().getListInventoryItems();
        List<InventoryItem> storeInventory = storeChoice.getInventoryItems();
        List<Integer> existingItems = fullInventory.stream().map(i -> i.getInventoryItemId()).collect(Collectors.toList());
        List<Integer> storeItemIds = storeInventory.stream().map(i -> i.getInventoryItemId()).collect(Collectors.toList());
        Boolean isValid = true;

        try {
            int inputAsInt = Integer.parseInt(input);
            boolean isExistingItem = existingItems.contains(inputAsInt);
            boolean isSoldAtStore = storeItemIds.contains(inputAsInt);

            if (!isExistingItem) {
                System.out.println("Invalid input: No reference found for itemId=" + inputAsInt);
                isValid = false;
            }
            if (isExistingItem && !isSoldAtStore) {
                System.out.printf("Invalid input: Item %d (%s) is not currently sold at store %s\n",
                        inputAsInt, sdm.getInventory().getInventoryItemById(inputAsInt).getItemName(), storeChoice.getStoreName());
                isValid = false;
            }
            return isValid;
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid input!");
            return false;
        }
    }


    private static void printPriceTableForStore(SDM sdm, Store storeChoice) {
        List<InventoryItem> fullInventory = sdm.getInventory().getListInventoryItems();
        List<InventoryItem> storeInventory = storeChoice.getInventoryItems();
        String prompt = "";
        prompt = prompt.concat(String.format("\n|%s| %-15s |%s| %-13s |", " Item-Id ", "Item-Name", " Purchase-Category ", "Price"));
        prompt = prompt.concat("\n----------------------------------------------------------------");

        for (InventoryItem item : fullInventory) {
            boolean isSoldAtStore = storeInventory.contains(item);

            String s2 = String.format("\n| %-7d | %-15s | %-18s| %-13s |",
                    item.getInventoryItemId(),
                    item.getItemName(),
                    item.getPurchaseCategory(),
                    isSoldAtStore ? storeChoice.getMapItemToPrices().get(item.getInventoryItemId()).toString() + " nis" : "not available"
            );

            prompt = prompt.concat(s2);
        }

        System.out.println(prompt);
        System.out.println("");

    }

    private static void printAvailableStores(SDM sdm) {
        System.out.printf("| %-8s | %-22s | %-5s |\n", "Store-id", "Name", "PPK");
        System.out.println("---------------------------------------------");
        for (Store store : sdm.getStores()) {
            System.out.printf("| %-8d | %-22s | %-5d |\n", store.getStoreId(), store.getStoreName(), store.getDeliveryPpk());
        }
    }

    private static List<Integer> getUserLocation(SDM sdmInstance) {
        List<SDMStore> listOfStores = sdmInstance.getListOfSDMStores();
        List<List<Integer>> listOfStoreLocations = sdmInstance.getListOfStoreLocations(listOfStores);

        boolean legalRange, spotNotTaken;
        Scanner src = new Scanner(System.in);

        while (true) {
            try {
                legalRange = true;
                spotNotTaken = true;

                System.out.println("What is your current location? (Please enter comma-separated whole numbers between [1,50])");
                System.out.println("Enter 'Q' to go back to main menu.");
                String input = src.nextLine();

                if (input.equalsIgnoreCase("q")) {
                    return Collections.singletonList(-1);
                }
                String[] lineVector = input.split(",");
                int x = Integer.parseInt(lineVector[0].trim());
                int y = Integer.parseInt(lineVector[1].trim());
                List<Integer> list = new ArrayList<>();
                list.add(x);
                list.add(y);


                if (x < 1 || x > 50 || y < 1 || y > 50) {
                    legalRange = false;
                    System.out.println("Invalid input: You entered " + "(" + list.get(0) + ", " + list.get(1) + "), but Coordinates must be in range [1,50]");
                }

                if (listOfStoreLocations.contains(list)) {
                    spotNotTaken = false;
                    System.out.println("Invalid input: The location (" + list.get(0) + ", " + list.get(1) + ") is already occupied by a store!");
                }
                if (legalRange && spotNotTaken) {
                    System.out.println("Great location!");
                    return list;
                }

            } catch (NumberFormatException nfe) {
                System.out.println("Number format exception: " + nfe.getMessage());
            } catch (NoSuchElementException nse) {
                System.out.println("No Such element exception: " + nse.getMessage());
            } catch (IllegalStateException ise) {
                System.out.println("Illegal state exception: " + ise.getMessage());
            } catch (IndexOutOfBoundsException obe) {
                System.out.println("Input must be two comma-separated whole numbers between 0 and 50!");
            }
        }
    }



    private static float getAmount(ePurchaseCategory purchaseCat) {
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
                    System.out.println("Invalid input: Quantity must be a positive number");
                } else if (purchaseCat == ePurchaseCategory.QUANTITY) {
                    System.out.println("Please enter order quantity: ");
                    res = in.nextFloat();

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
        for (Store store : stores) {
            System.out.println("\n===========================================================================\n");
            System.out.printf("| Store-id: %-3d | Store-name: %-10s |\n", store.getStoreId(), store.getStoreName());
            viewInventoryForStore(store);
            viewOrdersForStore(store);
            System.out.printf("\nPPK: %d", store.getDeliveryPpk());
            System.out.printf("\nTotal income from deliveries: %.2f\n", store.getTotalDeliveryIncome());
        }
        System.out.println("\n===========================================================================\n");
    }

    private static void viewOrdersForStore(Store store) {
        List<Order> orders = store.getOrders();
        if (orders.isEmpty()) {
            System.out.println("\nNo orders yet for store " + store.getStoreId());
        } else {
            System.out.printf("\nOrder history for store %d:\n", store.getStoreId());
            System.out.println("| Order Date  | Order id | Tot. num. items in cart | Cart subtotal | Delivery cost | Total   |");
            System.out.println("--------------------------------------------------------------------------------------------");
            store.getOrders().stream().forEach(o -> viewOrderDetails(o));
            System.out.println("");
        }
    }

    private static void viewOrderDetails(Order o) {
        String date = new SimpleDateFormat("dd/MM\thh:mm").format(o.getOrderDate());
        System.out.printf("| %-10s | %-8s | %-23d | %-13.2f | %-13.2f | %-7.2f |\n",
                date, o.getOrderId().toString(), o.getCartForThisOrder().getNumItemsInCart(), o.getCartTotal(), o.getDeliveryCost(), o.getCartTotal() + o.getDeliveryCost());
    }

    private static void viewAllItemsInSystem(SDM sdmInstance) {
        System.out.println("All items in system:");
        Inventory inventory = sdmInstance.getInventory();
        System.out.printf("\n| item-Id | %-15s | Purchase-Category | Amount of stores carrying item |   Ave price   | Total amount sold | ", "Item-Name");
        System.out.println("\n----------------------------------------------------------------------------------------------------------------------");

        for (InventoryItem item : inventory.getListInventoryItems()) {
            int numStoresCaryingItem = inventory.getMapItemsToStoresWithItem().get(item).size();
            float avePrice = inventory.getMapItemsToAvePrice().get(item);
            float totalAmountSold = inventory.getMapItemsToTotalSold().get(item);

            String s = String.format("%.2f", totalAmountSold);
            if (item.getPurchaseCategory() == ePurchaseCategory.WEIGHT)
                s = s.concat(" kgs");
            else if (item.getPurchaseCategory() == ePurchaseCategory.QUANTITY)
                s = s.concat(" pckgs");

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
        storeInventory.forEach(item -> {
            int id = item.getInventoryItemId();
            int price = store.getMapItemToPrices().get(id);
            float amountSold = store.getMapItemsToAmountSold().get(id);
            String s = String.format("%.2f", amountSold);
            if (item.getPurchaseCategory() == ePurchaseCategory.WEIGHT)
                s = s.concat(" kgs");
            else if (item.getPurchaseCategory() == ePurchaseCategory.QUANTITY)
                s = s.concat(" pckgs");

            System.out.printf("| %-7d | %-15s | %-18s| %-5d nis | %-11s |\n", id, item.getItemName(), item.getPurchaseCategory(),
                    price, s);

        });
    }


    private static void showPossibleCommands(boolean fileSuccessfullyLoaded) {
        System.out.println("\nPlease select an option from the following menu. Enter 'Q' to quit:");
        System.out.println("==============================================================================================");
        System.out.println("1) Load a new SDM file");

        if (fileSuccessfullyLoaded) {
            System.out.println("2) Display store details");
            System.out.println("3) View all items in system");
            System.out.println("4) Place an order");
            System.out.println("5) View Order History");
            System.out.println("6) Update inventory for store");
        }
        System.out.println("Q) Quit");
    }

    public static String readFilePath() {
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
}