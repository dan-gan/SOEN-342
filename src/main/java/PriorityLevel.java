public enum PriorityLevel {
        LOW, MEDIUM, HIGH;
        public String colored() {
        switch (this) {
            case LOW:
                return Colors.GREEN + "LOW" + Colors.RESET;
            case MEDIUM:
                return Colors.YELLOW + "MEDIUM" + Colors.RESET;
            case HIGH:
                return Colors.RED + "HIGH" + Colors.RESET;
            default:
                return this.name();
        }
    }
}