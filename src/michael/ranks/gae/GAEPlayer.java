package michael.ranks.gae;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;


@Entity(name = "pl")
@Unindexed
public class GAEPlayer {
	@Id String name;
	@Indexed int wins;
	@Indexed int loses;
}
