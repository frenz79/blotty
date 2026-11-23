public class AndCondition implements IFilterCondition {

    private final IFilterCondition left;
    private final IFilterCondition right;

    public AndCondition(
            IFilterCondition left,
            IFilterCondition right) {

        this.left = left;
        this.right = right;
    }

    @Override
    public boolean apply(Row row)
            throws FilterExpressionException {

        return left.apply(row)
            && right.apply(row);
    }
}
