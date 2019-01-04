import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import java.util.stream.IntStream;


/**
 * Created by Adrian on 21/09/2018.
 */
public class YahooAPI {

    public static void printClose(List<HistoricalQuote> historicalQuotes) {
        for (HistoricalQuote historicalQuote : historicalQuotes)
            System.out.println(historicalQuote.getClose());
    }

    public static void printTotal(List<HistoricalQuote> historicalQuotes) {
        BigDecimal totalClose = historicalQuotes
                .stream()
                .map(HistoricalQuote::getClose)
                .reduce(BigDecimal.ZERO // identity
                        , BigDecimal::add); // accumulator
        System.out.printf("Total close: %s\n", totalClose);
    }

    public static BigDecimal mean(List<HistoricalQuote> historicalQuotes) {
        //https://stackoverflow.com/questions/31881561/how-to-average-bigdecimals-using-streams
        BigDecimal[] totalWithCount = historicalQuotes.stream()
                .map(HistoricalQuote -> new BigDecimal[]{HistoricalQuote.getClose(), BigDecimal.ONE})
                // accumulator only
                // a[] = partial sum array
                // b[] = to be added array
                .reduce(new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO},         // identity
                        (a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(b[1])}   // accumulator
                );
        BigDecimal mean = totalWithCount[0].divide(totalWithCount[1], RoundingMode.HALF_UP);
        return mean;
    }

    public static BigDecimal varianceEqualWeighted(List<HistoricalQuote> history, BigDecimal mean) {
        BigDecimal totalProduct = IntStream
                .range(0, history.size())
                .mapToObj(i -> history.get(i).getClose().subtract(mean))
                .map(bd -> bd.multiply(bd))
                //.mapToObj(i -> history1.get(i).getClose().multiply(history2.get(i).getClose()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalProduct.divide(new BigDecimal(history.size() - 1), RoundingMode.HALF_UP);
    }


    public static void main(String[] args) {

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -5); // from 5 years ago

        try {
            Stock google = YahooFinance.get("GOOG", from, to, Interval.DAILY);
            List<HistoricalQuote> historyGoogle = google.getHistory();

            printClose(historyGoogle);
            printTotal(historyGoogle);
            BigDecimal mean = mean(historyGoogle);
            System.out.printf("Mean: %s\n", mean);
            BigDecimal variance = varianceEqualWeighted(historyGoogle, mean);
            System.out.printf("Variance: %s\n", variance);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
