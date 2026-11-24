public class IndexStatistics {

    long distinctValues;

    long totalRows;

    long nullRows;

    double selectivity(
        IGenericField value);
}
