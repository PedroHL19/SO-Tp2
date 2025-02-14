
public class Tools {
   public long findExponent(long num) {
      if (num == 1) {
         return 0;
      }
      return 1 + findExponent(num / 2);
   }

}
