import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A class containing methods to compute the return on investment (ROI) of trades throughout 2016.
 *
 * -There is no intra-day trading in this assignment.
 * -All prices given are the price at the close of trading for each day.
 * -We will assume all trades for a given day are made at this price.
 *
 * Stock prices obtained from Yahoo Finance.
 */
public class Investing {


    /**
     *
     *
     * Return the price of the stock at the given date as a double. Each line of the file contains 3 comma separated
     * values "date,price,volume" in the format "2016-03-23,106.129997,25703500" where the data is YYYY-MM-DD, the price
     * is given in USD and the volume is the number of shares traded throughout the day.
     *
     * Note: You don't have to interpret dates for this assignment and you can use the Sting's .equals method to 
     * compare dates whenever date comparisons are needed.
     *
     * @param stockFileName The filename containing the prices for a stock for each day in 2016
     * @param date          The date to lookup given in YYYY-MM-DD format
     * @return The price of the stock represented in stockFileName on the given date
     */
    public static double getPrice(String stockFileName, String date) {
        double result = 0.0;
        try{
            for(String line : Files.readAllLines(Paths.get(stockFileName))){
                String[] tokens = line.split(",");
                if (tokens[0].equals(date)) {
                    result = Double.parseDouble(tokens[1]);
                }
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return result;
    }


    /**
     *
     *
     * Return the cost of a single trade (stock price times number of shares).
     * If the trader is selling the shares this number should be negative.
     *
     * @param stockFileName  The filename containing the prices for a stock for each day in 2016
     * @param date           The date to lookup given in YYYY-MM-DD format
     * @param buyOrSell      Equals either "buy" or "sell" indicating the direction of the trade
     * @param numberOfShares the number of shares being bought or sold
     * @return The cost of a trade defined by the inputs
     */
    public static double costOfTrade(String stockFileName, String date, String buyOrSell, int numberOfShares) {
        double result = 0.0;
        try{
            for(String line : Files.readAllLines(Paths.get(stockFileName))){
                String[] tokens = line.split(",");
                if (tokens[0].equals(date)) {
                    if (buyOrSell.equalsIgnoreCase("buy")) {
                        result = Double.parseDouble(tokens[1]) * numberOfShares;
                        return result;
                    } else {
                        result = Double.parseDouble(tokens[1]) * numberOfShares * -1;
                        return result;
                    }

                }
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return 0.0;
    }


    /**
     *
     *
     * Determine whether or not the given trader made all valid trades. The file for a trader contains the details
     * of all trades throughout the year in chronological order with one trade on each line in the
     * format "date,buyOrSell,numberOfShares,tickerSymbol" (ex. "2012-05-18,buy,100,GOOG"). Each trader is given a
     * certain amount of starting cash and their cash will fluctuate throughout the year of trading. A trade is
     * invalid if any of the following are true:
     *
     * 1. The trader buys more shares of a stock than can be afforded with their current cash
     * 2. The trader sells more shares of a stock than they own
     *
     * @param traderFileName  The name of a file containing all the trades made by a trader throughout the year
     * @param startingCash    The amount of cash the trader has available at the start of the year
     * @param tickerFilenames Maps ticker symbols to the stock's filename
     * @return true if all trades are valid, false otherwise
     *
     *
     */

    public static boolean isTradingValid(String traderFileName, double startingCash, HashMap<String, String> tickerFilenames) {
        double totalCash = startingCash;
        double totalCost = 0.0;
        int totalShares = 0;

        int numberOfShares = 0;
        String date = "";
        String buyOrSell = "";
        String stockFileName = "";

        try{
            for(String line : Files.readAllLines(Paths.get(traderFileName))){
                String[] tokens = line.split(",");

                date = tokens[0];
                buyOrSell = tokens[1];
                numberOfShares = Integer.parseInt(tokens[2]);
                stockFileName = tickerFilenames.get(tokens[3]);
                totalCost = costOfTrade(stockFileName, date, buyOrSell, numberOfShares);

                if (buyOrSell.equalsIgnoreCase("buy")) {
                    if(totalCash < totalCost){
                        return false;
                    }
                    totalCash -= totalCost;
                    totalShares += numberOfShares;
                } else {
                    if(totalShares < numberOfShares){
                        return false;
                    }
                    totalCash -= totalCost;
                    // You cant keep adding shares in case of selling
                    totalShares -= numberOfShares;
                }
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return true;
    }



    /**
     * Compute the ROI of a given trader with a given starting cash.
     * Compute ROI as a fraction, as opposed to a percentage.
     * ROI is computed with the change in cash over the year and the value of all owned stocks.
     * The value of an owned stock is computed by it's price on the last trading day of the year ("2016-12-30").
     * You can assume all traders start the year owning no shares of any stock.
     *
     * Examples:
     * If a trader started with $1000 and ended the year with $1100 and $100 worth of stocks their ROI is 0.2
     * If a trader started with $8000 and ended the year with $6000 and $1500 worth of stocks, their ROI is -0.0625
     *
     * If the trader makes any invalid trades, return 0.0;
     *
     * @param traderFileName  The name of a file containing all the trades made by a trader throughout the year
     * @param startingCash    The amount of cash the trader has available at the start of the year
     * @param tickerFilenames Maps ticker symbols to the stock's filename
     * @return The ROI for the given trader and starting cash, or 0.0 is trading is invalid
     */
    public static double getTraderROI(String traderFileName, double startingCash, HashMap<String, String> tickerFilenames) {
        double endValue = startingCash;
        double stocks = 0.0;
        double  trader = 0.0;
        int numberOfStocks = 0;
        if(isTradingValid(traderFileName, startingCash, tickerFilenames)){
            try{
                for (String line: Files.readAllLines(Paths.get(traderFileName))){
                    String[] tokens = line.split(",");
                    String numberofShares = tokens[2];
                    numberOfStocks = Integer.parseInt(numberofShares);

                    endValue = endValue - costOfTrade(tickerFilenames.get(tokens[3]), tokens[0], tokens[1], numberOfStocks);
                    stocks += costOfTrade(tickerFilenames.get(tokens[3]), "2016-12-30", tokens[1], numberOfStocks);

                    trader = ((endValue + stocks) - startingCash)/startingCash;
                    System.out.println(trader);
                }
            }catch(IOException ex){
                ex.printStackTrace();
            }
            return trader;
        }
        return 0.0;
    }


    /**
     * Compute the ROI of the firm.
     *
     * Compute the total ROI given a map of traders and their starting cash
     * Note: Total ROI is not the average of each individual ROI. The total gain/loss of each trader must be considered
     * Example:
     * A trader starting with $5 and an ROI of 2.0 made $15. Another trader with $200 and ROI of -0.5 lost $100.
     * The average ROI is 1.25, but the total cash went from $205 to $120 for a total ROI of -0.4146
     *
     * @param traderFileNamesAndStartingCash A map with each traders filename as the keys and their corresponding
     *                                       starting cash as values
     * @param tickerFilenames                Maps ticker symbols to the stock's filename
     * @return The total ROI of the firm (all traders in traderFileNamesAndStartingCash)
     */
    public static double getTotalROI(HashMap<String, Double> traderFileNamesAndStartingCash,
                                     HashMap<String, String> tickerFilenames) {
        double totalCash = 0.0, endCash = 0.0, traderRoi, startingCash;
        for (String keySet : traderFileNamesAndStartingCash.keySet()) {
            traderRoi = getTraderROI(keySet, traderFileNamesAndStartingCash.get(keySet), tickerFilenames);
            startingCash = traderFileNamesAndStartingCash.get(keySet);
            endCash += traderRoi * startingCash + startingCash;
            totalCash += startingCash;
        }
        return (endCash - totalCash)/totalCash;
    }


    /**
     * Historical Algorithmic Trader: Create a sheet of trades to maximize ROI given x starting capital.
     *
     * @param outputFilename  The filename of the resulting trades file
     * @param startingCash    Cash at the beginning of 2016
     * @param tickerFilenames Maps ticker symbols to the stock's filename
     */
    public static void historicalAlgorithmicTrader(String outputFilename, double startingCash,
                                                   HashMap<String, String> tickerFilenames) {

        // Compute the optimal trading that would maximize ROI over the year with the given starting cash. You must
        // never buy more shares of a stock than you can afford or the trade is invalid. Write all trades to the
        // output file in the same format as the given trading files.
        //
        // Your algorithm must work for any stocks given in tickerFilenames which won't necessarily be the three used
        // for the assignment. You can assume the trading days will be the same (all of 2016).
        //
        // Points are all-or-nothing.
        //
        // Note: This is a true challenge!

        try{
            for(String key : tickerFilenames.keySet()){
                Object[] lines = Files.readAllLines(Paths.get(tickerFilenames.get(key))).toArray();
                // Reverse an array
                for (int i = 0; i < lines.length / 2; i++) {
                    Object temp = lines[i];
                    lines[i] = lines[lines.length - i - 1];
                    lines[lines.length - i - 1] = temp;
                }
                ArrayList<String> trades = new ArrayList<String>();
                int i = 0;
                while (i < lines.length) {

                    // minimum value
                    while (i < lines.length - 1 && Double.parseDouble(lines[i + 1].toString().split(",")[1]) <= Double.parseDouble(lines[i].toString().split(",")[1]))
                        i++;

                    // no possible best trade found as value kept decreasing from day one.
                    if (i == lines.length - 1)
                        break;

                    int minIndex = i++;
                    String[] tokens = lines[minIndex].toString().split(",");
                    double tradeCost = costOfTrade(tickerFilenames.get(key), tokens[0], "buy", 10);
                    if (startingCash >= tradeCost) {
                        trades.add(tokens[0] + ",buy,10,"+key);
                        startingCash -= tradeCost;
                    } else {
                        break;
                    }

                    // maximum value
                    while (i < lines.length && Double.parseDouble(lines[i].toString().split(",")[1]) >= Double.parseDouble(lines[i - 1].toString().split(",")[1]))
                        i++;

                    int maxIndex = i - 1;
                    tokens = lines[maxIndex].toString().split(",");
                    startingCash -= costOfTrade(tickerFilenames.get(key), tokens[0], "sell", 10);
                    trades.add(tokens[0] + ",sell,10,"+key);
                }
                Files.write(Paths.get(outputFilename+"_"+key+".csv"), trades);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    // The following testing code is to provided to help you get started testing your code. Provided are
    // methods to test getPrice and getTradePrice along with a sample call to getTraderROI.
    //
    // *You are encouraged to write similar testing code for the rest of the assignment

    public static void testGetPrice(String priceFile, String date, double confirmedPrice) {
        double computedPrice = getPrice(priceFile, date);
        System.out.println("getPrice(\"" + priceFile + "\", \"" + date + "\"); \nreturned : " + computedPrice);
        System.out.println("Confirmed price: " + confirmedPrice + "\n");
    }

    public static void testGetTradeCost(String priceFile, String date, String buyOrSell, int numberOfShares, double confirmedPrice) {
        double computedPrice = costOfTrade(priceFile, date, buyOrSell, numberOfShares);
        System.out.println("costOfTrade(\"" + priceFile + "\", \"" + date + "\", \"" + buyOrSell + "\", " + numberOfShares + "); \nreturned : " + computedPrice);
        System.out.println("Confirmed price: " + confirmedPrice + "\n");
    }

    public static void main(String[] args) {

        String priceFile = "historicPrices/AAPL_2016.csv";
        String date = "2016-03-04";
        double confirmedPrice = 103.010002;
        testGetPrice(priceFile, date, confirmedPrice);

        priceFile = "historicPrices/GOOG_2016.csv";
        date = "2016-01-04";
        confirmedPrice = 741.840027;
        testGetPrice(priceFile, date, confirmedPrice);

        priceFile = "historicPrices/GOOG_2016.csv";
        date = "2016-03-09";
        confirmedPrice = 705.23999;
        testGetPrice(priceFile, date, confirmedPrice);



        priceFile = "historicPrices/MSFT_2016.csv";
        date = "2016-10-11";
        String buyOrSell = "sell";
        int numberOfShares = 100;
        double confirmedCost = -5718.9999;
        testGetTradeCost(priceFile, date, buyOrSell, numberOfShares, confirmedCost);


        // sample usage of getTraderROI
        HashMap<String, String> tickerFilenames = new HashMap<String, String>();
        tickerFilenames.put("AAPL", "historicPrices/AAPL_2016.csv");
        tickerFilenames.put("GOOG", "historicPrices/GOOG_2016.csv");
        tickerFilenames.put("MSFT", "historicPrices/MSFT_2016.csv");

        double traderROI = getTraderROI("trades/lightTrader0.csv", 10000.0, tickerFilenames);
        double expectedROI = 0.06148999100000001;
        System.out.println("expected ROI: " + expectedROI);
        System.out.println("computed ROI: " + traderROI);
        System.out.println();

        System.out.println("If the price in the file and computed price do not match, please check your code in the " +
                "getPrice function.\nIf you are struggling, come to office hours. We are waiting for your " +
                "questions and are always happy to help.");

        HashMap<String, String> jk = new HashMap<String, String>();
        jk.put("MSFT", "historicPrices/MSFT_2016.csv");
        jk.put("GOOG", "historicPrices/GOOG_2016.csv");
        jk.put("AAPL", "historicPrices/AAPL_2016.csv");

        HashMap<String, Double> check1 = new HashMap<String, Double>();
        check1.put("trades/singleTrade2.csv", 100000.0);
        check1.put("trades/singleTrade0.csv", 100000.0);
        // needs to get 0.022725001499999998 or close to that values so about 0.022725001500000000

        HashMap<String, Double> check2 = new HashMap<String, Double>();
        check2.put("trades/heavyTrader2.csv", 500000.0);
        check2.put("trades/heavyTrader1.csv", 200000.0);
        check2.put("trades/heavyTrader3.csv", 500000.0);
        check2.put("trades/heavyTrader4.csv", 500000.0);
        check2.put("trades/heavyTrader0.csv", 200000.0);
        // needs to get 0.05469604447368434

        System.out.println("");
        System.out.println(getTotalROI(check1,jk));
        System.out.println(getTotalROI(check2,jk));

        //historic algorithm for trade
        historicalAlgorithmicTrader("trades/Best_Trade",1000000.0,tickerFilenames);
        System.out.println(getTraderROI("trades/Best_Trade_AAPL.csv",100000.0, tickerFilenames));
    }
}
