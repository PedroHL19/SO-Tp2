public class Page {
    private int pageNumber;
    private boolean referenced;  // Bit R
    private boolean modified;    // Bit M (dirty)
    private int lastAccess;     // Tempo do último acesso
    private boolean present;     // Se está na memória

    public Page(int pageNumber) {
        this.pageNumber = pageNumber;
        this.referenced = false;
        this.modified = false;
        this.lastAccess = 0;
        this.present = false;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public boolean isReferenced() {
        return referenced;
    }

    public void setReferenced(boolean referenced) {
        this.referenced = referenced;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public int getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(int time) {
        this.lastAccess = time;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}