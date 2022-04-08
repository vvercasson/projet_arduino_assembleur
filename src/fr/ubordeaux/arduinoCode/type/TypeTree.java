package fr.ubordeaux.arduinoCode.type;

public class TypeTree implements Type {

	private Tag tag;
	protected Type left;
	protected Type right;
	private Object data;
	private int size;
	private int offset;
	
	public TypeTree(Tag tag) {
		this.tag = tag;
		size();
	}

	public TypeTree(Tag tag, Type left) {
		this(tag);
		this.left = left;
	}

	public TypeTree(Tag tag, Type left, Type right) {
		this(tag, left);
		this.right = right;
	}

	public TypeTree(Tag tag, String name, Type left) {
		this(tag, left);
		this.data = name;
	}

	public TypeTree(Tag tag, Object data) {
		this(tag);
		this.data = data;
	}

	private void size() {
		switch (tag) {
		case BOOLEAN:
		case UINT8_T:
		case INT8_T:
		case ENUM:
			size = 2;
			break;
		case UINT16_T:
		case INT16_T:
			size = 2;
			break;
		case UINT32_T:
		case INT32_T:
			size = 4;
			break;
		}
	}
	
	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public Type getLeft() {
		return left;
	}

	@Override
	public Type getRight() {
		return right;
	}

	// Equivalent if same type
	@Override
	public boolean equivalent(Type type) {
		if (this.getClass() != type.getClass()) {
			return false;
		}
		if (left != null) {
			if (right == null) {
				return false;
			}
			else if (!left.equivalent(right)) {
				return false;
			}
		} else {
			if (right != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isBoolean() {
		return tag == Tag.BOOLEAN;
	}

	@Override
	public Tag getTag() {
		return tag;
	}

	public Object getData() {
		return data;
	}

	@Override
	public void cast(Tag tag) {
		this.tag = tag;
	}

}
