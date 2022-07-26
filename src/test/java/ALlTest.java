import com.mine.util.business.certificate.IdentityUtil;
import org.junit.jupiter.api.Test;

public class ALlTest {

    @Test
    public void testIdentityUtil(){
        IdentityUtil.ValidatedResult validatedResult = IdentityUtil.validate18Idcard("11111");
        System.out.println(validatedResult);
    }

}
