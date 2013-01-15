package evil;

public class Pair {
	public final String a;
	public final String b;
	
	public Pair(String a, String b) {
		if(a.compareTo(b) <= 0) {
			this.a = a;
			this.b = b;
		} else {
			this.a = b;
			this.b = a;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Pair) {
			Pair p = (Pair) o;
			return p.a.equals(a) && p.b.equals(b);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return a.hashCode() * 31 + b.hashCode() * 7;
	}
}
