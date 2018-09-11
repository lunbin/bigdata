import org.junit.Assert;
import org.junit.Test;

public class BinarySearchTest {
  @Test
  public void testBinarySearch() {
    int[] arr = {1,2,3,5};
    int i = BinarySearch.binarySearch(arr,3);
    Assert.assertEquals(2,i);
  }
}

