public class Configuration {
    private int totalPages;      // p: número de páginas do espaço de endereçamento virtual
    private int totalFrames;     // m: número de molduras da memória
    private int clockInterval;   // c: ciclo de relógio do bit R
    private int pageNumber;      // número da página sendo acessada
    private int accessTime;      // t: momento do acesso
    private boolean isWrite;     // true para W (escrita), false para R (leitura)

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void setTotalFrames(int totalFrames) {
        this.totalFrames = totalFrames;
    }

    public int getClockInterval() {
        return clockInterval;
    }

    public void setClockInterval(int clockInterval) {
        this.clockInterval = clockInterval;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(int accessTime) {
        this.accessTime = accessTime;
    }

    public boolean isWrite() {
        return isWrite;
    }

    public void setWrite(boolean write) {
        isWrite = write;
    }
}