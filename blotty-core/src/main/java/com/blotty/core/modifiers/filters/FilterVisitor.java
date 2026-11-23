interface FilterVisitor<T> {

    T visit(BinaryCondition c);

    T visit(UnaryCondition c);

    T visit(AndCondition c);

    T visit(OrCondition c);
}
