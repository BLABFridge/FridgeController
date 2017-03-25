import java.util.LinkedList;

class Database<T> {

	private LinkedList<T> items;

	public Database(){
		items = new LinkedList<T>();
	}

	public synchronized void add(T i){
		items.add(i);
		notifyAll();
	}

	public T get(int i){
		return items.get(i);
	}

	public int size(){
		return items.size();
	}

	public int indexOf(T t){
		for (int i = 0; i < items.size() ; ++i) {
			ReaderClass.println(items.get(i).equals(t) + "");
			if(items.get(i).equals(t)) return i;
		}
		return -1;
	}

	public synchronized void remove(T t){
		items.remove(t);
		notifyAll();
	}

	public synchronized void remove(int i){
		items.remove(i);
		notifyAll();
	}

	public int numberOfInstances(T t){
		int n = 0;
		for (int i = 0; i < items.size(); ++i) {
			if (items.get(i).equals(t)) ++n;
		}
		return n;
	}
	
}