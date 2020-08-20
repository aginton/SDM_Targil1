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
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIMain {

    public static void main(String[] args) throws IOException {
        boolean fileExists = false;
        boolean isSDMLoaded = false;
        boolean wantsToQuit = false;
        SDM sdmInstance = new SDM();

        SuperDuperMarketDescriptor sdmLoaded;

        // create an object of Scanner class
        Scanner scanner = new Scanner(System.in);

        // ask user to enter operator
        String operator;


        while (!wantsToQuit) {
            System.out.println("");
            showPossibleCommands(isSDMLoaded);
            operator = scanner.nextLine().toLowerCase();
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
                        printDetailsOfSDM(sdmInstance.getSuperDuperMarketDescriptor());
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



    private static void viewOrderHistory(SDM sdmInstance) {
        List<Order> history = sdmInstance.getOrderHistory().getOrders();
        System.out.println("Order history:");
        if (!history.isEmpty()) {

            System.out.println("==============================================================================================");
            for (Order order : history) {
                int orderId = order.getOrderId();
                Date orderDate = order.getOrderDate();
                String date = new SimpleDateFormat("dd/MM\tHH:mm").format(orderDate);
                int storeId = order.getStore().getStoreId();
                String storeName = order.getStore().getStoreName();
                float deliveryCost = order.getDeliveryCost();
                Cart cart = order.getCartForThisOrder();
                float cartTotal = cart.getCartTotalPrice();
                float total = cartTotal + deliveryCost;

                System.out.printf("| Order-id: %d | %-10s | Store-Id: %-5d | %-13s |\n\n", orderId, date, storeId, storeName);
                printCartDetails(cart);
                System.out.printf("\nTotal number of items: %d", order.getCartForThisOrder().getNumItemsInCart());
                System.out.printf("\nTotal types of items: %d", order.getCartForThisOrder().getNumberOfTypesOfItemsInCart());
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

//
//    private static void testThisMethod(SDM sdmInstance) {
//
//        for (SDMItem i : sdmInstance.getListOfSDMItems()) {
//            Map<Integer, Integer> itemPrices = sdmInstance.getMapForStoresThatSellItem(i.getId());
//
//            itemPrices.forEach((k, v) -> System.out.println("Price at storeId= " + k + ": " + v));
//        }
//    }


    private static void placeAnOrder(SDM sdmInstance) {
        List<Store> listOfStores = sdmInstance.getStores();
        Store storeChoice = null;
        List<Integer> userLocation = new ArrayList<>();
        int userInput;
        float amount;
        Cart cart = new Cart();
        Scanner in = new Scanner(System.in);
        String input;

        //1. Show stores and ask user for Store id
        userInput = getStoreIdFromUser(sdmInstance);
        if (userInput == -1)
            return;

        storeChoice = listOfStores.get(userInput - 1);

        //2. Ask user for date and time
        Date orderDate = getOrderDateFromUser();
        if (orderDate == null)
            return;

        //3. ask user for their location
        userLocation = getUserLocation(sdmInstance);
        if (userLocation.contains(-1))
            return;

        float distance = getDistance(userLocation, storeChoice.getStoreLocation());
        float deliveryCost = distance*storeChoice.getDeliveryPpk();

        //4. Choosing items to buy
        while (true) {
            System.out.println("\nTo confirm cart purchase, enter 'confirm'. To add an item to your cart, enter 'add'. To cancel order, enter 'Q'");
            System.out.println("=======================================================================");
            System.out.println("Store: " + storeChoice.getStoreName());
            String pattern = "dd/MM HH:mm";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String formattedDate = simpleDateFormat.format(orderDate);
            System.out.println("Order date: " + formattedDate);
            System.out.println("My location: (" + userLocation.get(0) + ", " + userLocation.get(1) + ")");
            System.out.printf("PPK: %d\n", storeChoice.getDeliveryPpk());
            System.out.printf("Distance from store: %.2f\n", distance);
            System.out.println("\nCart summary:");
            printCartDetails(cart);
            System.out.printf("\nDelivery fee: %.2f", deliveryCost);
            System.out.println("\nCart subtotal:" + cart.getCartTotalPrice());
            System.out.print("----------------");
            float total = cart.getCartTotalPrice()+deliveryCost;
            System.out.printf("\nTotal: %.2f nis",total);

            System.out.println("");

            input = in.nextLine();
            switch (input.toLowerCase()){
                case "confirm":
                    System.out.println("Order confirmed! (:");

                    Order order = new Order(storeChoice, userLocation, orderDate, deliveryCost, cart);
                    sdmInstance.addNewOrder(storeChoice, order);
                    return;

                case "add":
                    //ask user to enter ID for item to purchase
                    int priceID = getPriceIdFromUser(sdmInstance, storeChoice);

                    if (priceID == -1)
                        return;

                    int price = storeChoice.getMapItemToPrices().get(priceID);

                    //ask user to enter amount
                    InventoryItem itemChosen = storeChoice.getInventoryItemById(priceID);

                    amount = getAmount(itemChosen.getPurchaseCategory());
                    CartItem cartItem = new CartItem(itemChosen, amount, price);
                    cart.add(cartItem);


                    break;

                case "q":
                    return;

                default:
                    System.out.println("Invalid input! ):");
            }
        }
    }

    private static Date getOrderDateFromUser() {

        Date date = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM-kk:mm");
        dateTimeFormat.setLenient(false);
        Scanner in = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("Please enter date and time for delivery as dd/MM-hh:mm");

                String input = in.nextLine();
                if (input.equalsIgnoreCase("q"))
                    return null;

                date = dateTimeFormat.parse(input);

                if (date != null)
                    return date;
            } catch (ParseException e) {
                System.out.println("Date and time are not in the correct pattern");
            }
        }
    }

    private static float getDistance(List<Integer> userLocation, List<Integer> storeLocation) {
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


    private static void printCartDetails(Cart cart) {
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


    private static int getPriceIdFromUser(SDM sdm, Store storeChoice) {
        List<InventoryItem> fullInventory = sdm.getInventory().getListInventoryItems();
        List<InventoryItem> storeInventory = storeChoice.getInventoryItems();


        List<Integer> existingItems = fullInventory.stream().map(i-> i.getInventoryItemId()).collect(Collectors.toList());
        List<Integer> storeItemIds = storeInventory.stream().map(i-> i.getInventoryItemId()).collect(Collectors.toList());

        System.out.println("Please enter the Id for the item you wish to purchase: ");

        while (true) {
            printPriceTableForStore(sdm,storeChoice);

            int priceId = getIntFromUser();
            //int priceId = getIntFromUser(prompt);

            if (priceId == -1)
                return -1;

            if (!existingItems.contains(priceId)) {
                System.out.println("aaaaaaaa");
                System.out.println("Invalid input: No reference found for itemId=" + priceId);
            } else if (existingItems.contains(priceId) && !storeItemIds.contains(priceId)) {
                System.out.println("The item you selected is not currently available at this store. ");
            } else
                return priceId;
        }
    }

    private static void printPriceTableForStore(SDM sdm, Store storeChoice) {

        List<InventoryItem> fullInventory = sdm.getInventory().getListInventoryItems();
        List<InventoryItem> storeInventory = storeChoice.getInventoryItems();

        String prompt = "";
        prompt = prompt.concat(String.format("\n|%s| %-15s |%s| %-13s |"," Item-Id ", "Item-Name"," Purchase-Category ", "Price"));
        prompt = prompt.concat("\n----------------------------------------------------------------");


        //TODO: maybe change structure of inventory to hold items instead?
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
        //Create prompt message
        String prompt = "\nWhich store would you like to order from? Please enter the store-id from the following options. Enter 'Q' at anytime to cancel order: ";
        System.out.println(prompt);

        System.out.printf("| %-8s | %-22s | %-5s |\n", "Store-id", "Name", "PPK");
        System.out.println("---------------------------------------------");
        for (Store store : sdm.getStores()) {
            //availableStores = availableStores.concat("\n" + i + ") Store-id = " + store.getStoreId() + ",\tName = " + store.getStoreName() + ",\tPPK = " + store.getDeliveryPpk());
            System.out.printf("| %-8d | %-22s | %-5d |\n", store.getStoreId(), store.getStoreName(), store.getDeliveryPpk());
        }
    }

    private static int getStoreIdFromUser(SDM sdmInstance) {
        List<Store> listOfStores = sdmInstance.getStores();
        List<Integer> storeIds = listOfStores.stream().map(i->i.getStoreId()).collect(Collectors.toList());

        Scanner in = new Scanner(System.in);
        boolean isValidStoreId = false;

        int userInput;

        while (true) {
            try {
                printAvailableStores(sdmInstance);
                userInput = getIntFromUser();
                if (userInput == -1)
                    return -1;

                if (!storeIds.contains(userInput))
                    System.out.println("Invalid Input: could not find existing store with id " + userInput);

                else if (storeIds.contains(userInput))
                    return userInput;

            } catch (InputMismatchException e) {
                System.out.println("Invalid input!");
            } catch (NumberFormatException nfe) {
                System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
                //System.out.println(prompt);
            }
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
                List<Integer> list = Stream.of(input.split(",")).map(Integer::parseInt).collect(Collectors.toList());

                if (input.equalsIgnoreCase("q")) {
                    return Collections.singletonList(-1);
                }

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
            System.out.println("Cancel operation? (Y/N)");
            String input = in.nextLine();
            if (input.equalsIgnoreCase("y"))
                return true;

            if (input.equalsIgnoreCase("n"))
                return false;
        }
    }

    private static float getAmount(ePurchaseCategory purchaseCat) {
        boolean isValidQuantity = false;
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
                in.nextLine();
            }
        }
    }


    //TODO: Make shorter and simpler
    private static int getIntFromUser() {
        Scanner in = new Scanner(System.in);
        int userInput;
        String userInputStr;
        boolean comingFromCancel = false;

        while (true) {
            try {
                comingFromCancel = false;
                userInputStr = in.nextLine();
                if (userInputStr.equalsIgnoreCase("q")) {
                    if (checkUserWantsToCancelOrder()) {
                        return -1;
                    }
                    comingFromCancel = true;
                }
                userInput = Integer.parseInt(userInputStr);
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input!");
            } catch (NumberFormatException nfe) {
                if (!comingFromCancel) {
                    System.out.println("Invalid input: Please only enter whole numbers, or 'Q' to quit");
                    comingFromCancel = false;
                }
            }
        }
        return userInput;
    }


//    //TODO: Make shorter and simpler
//    private static int getIntFromUser(String s) {
//        Scanner in = new Scanner(System.in);
//        int userInput;
//        String userInputStr;
//        boolean comingFromCancel = false;
//
//        while (true) {
//            try {
//                comingFromCancel = false;
//                System.out.println(s);
//                userInputStr = in.nextLine();
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
    }

    private static void viewOrdersForStore(Store store) {
        List<Order> orders = store.getOrders();
        System.out.println("\nOrders: ");
        if (orders.isEmpty()){
            System.out.println("No orders yet for store " + store.getStoreId());
        } else{
            System.out.printf("Order history for store %d:\n", store.getStoreId());
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
        System.out.println("\nInventory: ");
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
            System.out.println("6) Print File Details");
            System.out.println("Q) Quit");
            System.out.println("9) Test Method");
        }
    }

    public static String readFilePath() {
        boolean isValidPath = false;
        String s;
        File f;
        Scanner in = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("Enter file path. Press 'Q' to cancel anytime: ");
                String fileName = in.nextLine();

                if (fileName.equalsIgnoreCase("q"))
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


}
