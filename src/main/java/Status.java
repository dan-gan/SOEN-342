public enum Status {
        OPEN, COMPLETED, CLOSED;
        
        public String colored() {
        switch (this) { 
            case OPEN:
                return Colors.GREEN + "OPEN" + Colors.RESET;
            case COMPLETED:
                return Colors.YELLOW + "COMPLETED" + Colors.RESET;
            case CLOSED:
                return Colors.RED + "CLOSED" + Colors.RESET;
            default:
                return this.name();
        }
    }
    
    @Override
    public String toString() {
        return colored();  // now println() automatically prints color
    }
}