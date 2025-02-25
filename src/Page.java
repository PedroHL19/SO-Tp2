public class Page {
    private long pageNumber;     
    private boolean referenced;
    private boolean modified;   
    private long lastAccess;      
    private boolean present;     

    public Page(long pageNumber) {  
        this.pageNumber = pageNumber;
        this.referenced = false;
        this.modified = false;
        this.lastAccess = 0;
        this.present = false;
    }

    public long getPageNumber() {  
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

    public long getLastAccess() {  
        return lastAccess;
    }

    public void setLastAccess(long time) {  
        this.lastAccess = time;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}