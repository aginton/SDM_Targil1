package ui;


import Orders.Order;
import SDM.SDM;
import jaxb.schema.generated.SDMItem;
import jaxb.schema.generated.SDMSell;
import jaxb.schema.generated.SDMStore;
import jaxb.schema.generated.SuperDuperMarketDescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String s = "";
        String xmlFileName = "";
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
                    String tmpString = readFilePath();
//                    String tmpString = readFilePath(bufferedReader);

                    if (!tmpString.equalsIgnoreCase("q")) {
                        //SuperDuperMarketDescriptor dummySDM;
                        //System.out.println("User entered path: " + tmpString);

                        if (sdmInstance.tryLoadingSDMObjectFromFile(tmpString)) {
                            System.out.println("SDM-File loaded successfully!");
                            isSDMLoaded = true;
                        } else {
                            System.out.println("\nUnable to load file:");
                            System.out.println(sdmInstance.getLoadingErrorMessage());
                        }

                    }
                    break;

                case "2":
                    if (isSDMLoaded) {
                        printStoreInformation(sdmInstance);
                    }
                    break;


                case "3":

                    break;

                case "4":
                    placeAnOrder(sdmInstance);
                    break;

                case "5":
                    break;

                case "6":
                    printDetailsOfSDM(sdmInstance.getSuperDuperMarketDescriptor());
                    break;

                case "q":
                    System.out.println("Goodbye!");
                    return;

                case "9":
                    testThisMethod(sdmInstance);
                    break;

                default:
                    System.out.println("Invalid input. " + operator + " does not correspond to any command!");
            }


        }
    }

    private static void testThisMethod(SDM sdmInstance) {

        for (SDMItem i : sdmInstance.getListOfSDMItems()) {
            Map<Integer, Integer> itemPrices = sdmInstance.getMapForStoresThatSellItem(i.getId());

            itemPrices.forEach((k, v) -> System.out.println("Price at storeId= " + k + ": " + v));
        }
    }

    private static void placeAnOrder(SDM sdmInstance) {
        int userInput;
        float amount, cartTotal = 0, deliveryCost = 0;
        Order order = new Order();
        HashMap<Integer, HashMap<String, Object>> myCart = new HashMap<>();
        HashMap<String, Object> myOrder = new HashMap<>();
        Scanner in = new Scanner(System.in);
        String input;


        List<SDMStore> listOfStores = sdmInstance.getListOfSDMStores();
        SDMStore storeChoice;
        List<Integer> userLocation = new ArrayList<>();

        //ask user for Store id
        userInput = getStoreIdFromUser(sdmInstance);
        if (userInput == -1)
            return;

        storeChoice = listOfStores.get(userInput - 1);
        myOrder.put("StoreId", storeChoice.getId());

        //TODO: Get date/time in more user-friendly way
        Date orderDate = getOrderDateFromUser();
        if (orderDate == null)
            return;
        //System.out.println("getOrderDate returned: " + orderDate);

        //ask user for their location
        userLocation = getUserLocation(sdmInstance);
        if (userLocation.contains(-1))
            return;

        //TODO?: make data types more consistent (use either only doubles or only floats)
        double distance = getDistance(userLocation, sdmInstance.getStoreLocation(storeChoice));

        while (true) {
            HashMap<String, Object> itemDetails;

            System.out.println("\nTo confirm cart purchase, enter 'confirm'. To add an item to your cart, enter 'add'. To cancel order, enter 'Q'");
            System.out.println("=======================================================================");
            System.out.println("Store: " + storeChoice.getName());
            System.out.println("Order Date: " + orderDate);
            System.out.println("My location: (" + userLocation.get(0) + ", " + userLocation.get(1) + ")");
            System.out.println("\nCart subtotal:" + cartTotal);
            System.out.println("Total: " + cartTotal+deliveryCost);
            System.out.println("\nCart summary:");
            printCartDetails(myCart);

            input = in.nextLine();
            switch (input.toLowerCase()){
                case "confirm":
                    System.out.println("Order confirmed! (:");
                    //TODO: Save order somehow
                    return;

                case "add":
                    //ask user to enter ID for item to purchase
                    int priceID = getPriceIdFromUser(sdmInstance, storeChoice);
                    if (priceID == -1)
                        return;

                    int price = sdmInstance.getPriceForItemAtStore(priceID, storeChoice);

                    //ask user to enter amount
                    SDMItem itemChosen = sdmInstance.getSDMItemById(priceID);
                    amount = getAmount(itemChosen);
                    cartTotal += (price*amount);

                    if (myCart.containsKey(priceID)){
                        Float oldAmount = (float) myCart.get(priceID).get("amount");
                        myCart.get(priceID).put("amount", oldAmount+amount);
                        myCart.get(priceID).put("itemTotalCost", (oldAmount+amount)*price);
                    }

                    //if (!myCart.containsKey(priceID))
                    else{
                        itemDetails = new HashMap<>();
                        itemDetails.put("name", itemChosen.getName());
                        itemDetails.put("purchaseCategory", itemChosen.getPurchaseCategory());
                        itemDetails.put("amount", amount);
                        itemDetails.put("price", price);
                        itemDetails.put("itemTotalCost", price*amount);
                        myCart.put(priceID, itemDetails);
                    }

                    break;

                case "q":
                    return;

                default:
                    System.out.println("Invalid input! ):");
            }
        }
    }

    private static double getDistance(List<Integer> userLocation, List<Integer> storeLocation) {
        if (userLocation.size() != 2 || storeLocation.size() != 2){
            System.out.println("Error: Input lists must each contain 2 points!");
            return -1;
        }


        int xDelta = userLocation.get(0) -storeLocation.get(0);
        int yDelta = userLocation.get(1) -storeLocation.get(1);
        return Math.sqrt((xDelta*xDelta)+(yDelta*yDelta));
    }

    //https://kodejava.org/how-do-i-align-string-print-out-in-left-right-center-alignment/
    //https://howtodoinjava.com/java/string/left-right-or-center-align-string/
    //https://www.java67.com/2014/06/how-to-format-float-or-double-number-java-example.html#:~:text=format(%22%25.,float%20data%20type%20in%20Java.
    //https://www.homeandlearn.co.uk/java/java_formatted_strings.html
    //https://stackoverflow.com/questions/15961130/align-printf-output-in-java
    private static void printCartDetails(HashMap<Integer, HashMap<String, Object>> myCart) {
//        System.out.println("Just entered printCartDetails");
//        System.out.println("Is myCart empty?: " + (myCart==null));

       if (myCart != null){
           myCart.forEach((k,v)->{
               String name = (String) v.get("name");
               String pCat = (String) v.get("purchaseCategory");
               int price = (int) v.get("price");
               float amount = (float) v.get("amount");
               float itemTotalCost = (float) v.get("itemTotalCost");
               System.out.printf("\n%-3d| %-15s| unit price=%-3d| %8s: %-5.2f| cost=%-5.2f", k, name, price, pCat, amount, itemTotalCost);
           });
       }
    }

    private static Date getOrderDateFromUser() {

        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM hh:mm");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
        Scanner in = new Scanner(System.in);


        while (true) {
            try {
                System.out.println("Please enter day and month for delivery as dd/MM hh:mm");

                String input = in.nextLine();
                if (input.equalsIgnoreCase("q"))
                    return null;


                date = dateFormat.parse(input);
                //System.out.println("date==null?: " + (date == null));

                if (date != null)
                    return date;
            } catch (ParseException e) {
                System.out.println("Parsing exception!");
            }
        }
    }

    public Date validateDateFormat(String dateToValdate, String formatToValidate) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
        //To make strict date format validation
        formatter.setLenient(false);
        Date parsedDate = null;
        try {
            parsedDate = formatter.parse(dateToValdate);
            System.out.println("++validated DATE TIME ++" + formatter.format(parsedDate));

        } catch (ParseException e) {
            //Handle exception
        }
        return parsedDate;
    }


    private static int getPriceIdFromUser(SDM sdmInstance, SDMStore storeChoice) {
        List<Integer> listOfItemIds = sdmInstance.getListOfItemIds(sdmInstance.getListOfSDMItems());
        List<Integer> listOfPricedItemIds = sdmInstance.getListOfPriceIdsForStore(storeChoice);
        String prompt = "Please enter the Id for the item you wish to purchase: ";
        int priceId;

        //Create string listing items and their details
        for (SDMItem item : sdmInstance.getListOfSDMItems()) {
            prompt = prompt.concat("\n\titemId= " + item.getId() + ", item-Name= " + item.getName() + ", PurchaseCategory= " + item.getPurchaseCategory());

            if (listOfPricedItemIds.contains(item.getId())) {
                int price = sdmInstance.getPriceForItemAtStore(item.getId(), storeChoice);
                prompt = prompt.concat(", price= " + price);
            } else {
                prompt = prompt.concat(", (item not sold at store)");
            }
        }


        while (true) {
            //System.out.println(prompt);
            priceId = getIntFromUser(prompt);

            if (priceId == -1)
                return -1;

            if (!listOfItemIds.contains(priceId)) {
                System.out.println("aaaaaaaa");
                System.out.println("Invalid input: Please enter an id from list of items:");
            } else if (listOfItemIds.contains(priceId) && !listOfPricedItemIds.contains(priceId)) {
                System.out.println("The item you selected is not currently available at this store. ");
            } else
                return priceId;
        }
    }

    private static int getStoreIdFromUser(SDM sdmInstance) {
        List<SDMStore> listOfStores = sdmInstance.getListOfSDMStores();
        List<Integer> listOfStoreIds = sdmInstance.getListOfStoreIds(listOfStores);
        Scanner in = new Scanner(System.in);
        boolean isValidStoreId = false;

        int userInput;

        //Create prompt message
        String prompt = "\nWhich store would you like to order from? Please enter the store-id from the following options. Enter 'Q' at anytime to cancel order: ";
        int i = 1;
        for (SDMStore store : listOfStores) {
            prompt = prompt.concat("\n" + i + ") Store-id= " + store.getId() + ",\tname= " + store.getName() + ",\tPPK= " + store.getDeliveryPpk());
            i++;
        }

        while (true) {
            try {
                userInput = getIntFromUser(prompt);
                if (userInput == -1)
                    return -1;

//                int num = Integer.parseInt(userInput);
                if (!listOfStoreIds.contains(userInput))
                    System.out.println("Invalid Input: could not find existing store with id " + userInput);

                else if (listOfStoreIds.contains(userInput))
                    return userInput;

            } catch (InputMismatchException e) {
                System.out.println("Invalid input!");
                //in.nextLine();
            } catch (NumberFormatException nfe) {
                System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
                System.out.println(prompt);
                //in.nextLine();
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
                System.out.println("What is your current location? (Please enter comma-separated whole numbers between [0,50])");
                String input = src.nextLine().trim();
                List<Integer> list = Stream.of(input.split(",")).map(Integer::parseInt).collect(Collectors.toList());

                if (input.equalsIgnoreCase("q")) {
                    return Collections.singletonList(-1);
                }

                if (list.size() > 2){
                    legalSize = false;
                    System.out.println("Invalid input: too many points entered!");
                }

                if (list.get(0) < 0 || list.get(0) > 50 || list.get(1) < 0 || list.get(1) > 50) {
                    legalRange = false;
                    System.out.println("Invalid input: You entered " + "(" + list.get(0) + ", " + list.get(1) + "), but Coordinates must be in range [0,50]");
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

    private static float getAmount(SDMItem itemChosen) {
        boolean isValidQuantity = false;
        float res;
        Scanner in = new Scanner(System.in);
        DecimalFormat df;
        String purchaseCat = itemChosen.getPurchaseCategory();

        while (true) {
            try {
                if (purchaseCat.equals("Weight")) {
                    df = new DecimalFormat("0.00");
                    System.out.println("Please enter order weight in kgs: ");
                    res = in.nextFloat();
                    if (res > 0)
                        return Float.valueOf(df.format(res));
                } else if (purchaseCat.equals("Quantity")) {
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
    private static int getIntFromUser(String s) {
        Scanner in = new Scanner(System.in);
        int userInput;
        String userInputStr;
        boolean comingFromCancel = false;

        while (true) {
            try {
                comingFromCancel = false;
                System.out.println(s);
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
                //in.nextLine();
            } catch (NumberFormatException nfe) {
                if (!comingFromCancel) {
                    System.out.println("Invalid input: Please only enter whole numbers, or 'Q' to quit");
                    comingFromCancel = false;
                }
            }
        }
        return userInput;
    }


    private static void printStoreInformation(SDM sdm) {

        for (SDMStore store : sdm.getListOfSDMStores()) {
            System.out.println(sdm.showStoreInformation(store));
        }
    }

    private static void showPossibleCommands(boolean fileSuccessfullyLoaded) {
        //System.out.println("Press '1' to load a new file, or 'Q' to quit at any time");
        System.out.println("\nPlease select an option from the following menu. Enter 'Q' to quit:");
        System.out.println("==========================================================================================");
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


//    public static String readFilePath(BufferedReader bufferedReader) throws IOException {
//        boolean isValidPath = false;
//        String s;
//        File f;
//
//        do {
//            System.out.println("Please enter a valid file path. Press 'Q' to cancel anytime: ");
//            s = bufferedReader.readLine();
//
//            //System.out.println("Value of s: " + s);
//
//            if (s.equalsIgnoreCase("c")){
//                return "";
//            }
//
//            else if (s.length() <= 3 || !s.substring(s.length() - 3).toLowerCase().equals("xml")){
//                System.out.println("Error: Invalid Path. Please enter full path to XML file");
//            }
//
//            else{
//                f = new File(s);
//
//                if (f.exists())
//                    isValidPath = true;
//            }
//        } while (!isValidPath);
//        return s;
//    }

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
                //sells.concat("ajkbj;lkdjsfa;lkjdafd;sj");
                sells = sells.concat(" {item-id=" + itemSold.getItemId() + ", price=" + itemSold.getPrice() + "},");
            }
            sells = sells.concat("]");
            System.out.println("Store-Id=" + store.getId() + ", store-name= " + store.getName() + ", location= " + loc + ", " + sells);
        }
    }


}
