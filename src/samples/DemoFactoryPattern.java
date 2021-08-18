package samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;


public class DemoFactoryPattern
{
	public static void main(String... args)
	{
		try
		{
			Factory factory = new Factory();
			factory.userService = caller -> new MockUserService(new User("dave", "asasa sasas asasa sasas"), new User("steve", "ghghg hghgh ghghg hgh ghghg hgh"));
			factory.background = caller -> Color.RED;
			factory.foreground = caller -> Color.BLUE;
			factory.style = caller -> new Style(factory);
			factory.textSize = caller -> caller instanceof Style ? 96f : 24f;
			factory.userPanel = caller -> new UserPanel(factory);

			UserPanel panel = factory.userPanel.apply(null);

			JFrame frame = new JFrame();
			frame.add(panel);
			frame.setSize(1024, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	static class Factory
	{
		Function<Object, UserService> userService;
		Function<Object, Color> background;
		Function<Object, Color> foreground;
		Function<Object, Style> style;
		Function<Object, Float> textSize;
		Function<Object, UserPanel> userPanel;
	}


	static class Style
	{
		Color mText;
		Color mBackground;
		float mTextSize = 12f;


		public Style(Factory aFactory)
		{
			mText = aFactory.foreground.apply(this);
			mBackground = aFactory.background.apply(this);
			mTextSize = aFactory.textSize.apply(this);
		}


		public Style(Color aText, Color aBackground)
		{
			mText = aText;
			mBackground = aBackground;
		}
	}


	static class UserPanel extends JPanel
	{
		private UserService mUserService;
		private Style mStyle;
		private float mTextSize = 12f;
		private User mUser;


		public UserPanel(Factory aFactory)
		{
			mUserService = aFactory.userService.apply(this);
			mStyle = aFactory.style.apply(this);
			mTextSize = aFactory.textSize.apply(this);

			buildForm();
		}


		public void buildForm()
		{
			JList<User> list = new JList<>(mUserService.getUsers());
			list.setFont(list.getFont().deriveFont(mTextSize));

			JTextArea text = new JTextArea();
			text.setForeground(mStyle.mText);
			text.setBackground(mStyle.mBackground);
			text.setFont(text.getFont().deriveFont(mStyle.mTextSize));
			text.setLineWrap(true);

			list.addListSelectionListener(aEvent ->
			{
				if (mUser != null && !mUser.mDescription.equals(text.getText()))
				{
					mUser.mDescription = text.getText();
					mUserService.save(mUser);
				}

				mUser = list.getModel().getElementAt(list.getSelectedIndex());

				text.setText(mUser.mDescription);
			});

			super.setLayout(new BorderLayout());
			super.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(list), new JScrollPane(text)));
		}
	}


	static class MockUserService extends UserService
	{
		User[] mUsers;


		public MockUserService(User... aUsers)
		{
			mUsers = aUsers;
		}


		@Override
		public User[] getUsers()
		{
			return mUsers;
		}


		@Override
		public void save(User aUser)
		{
			System.out.println("saved user: " + aUser + "=" + aUser.mDescription);
		}
	}


	static class UserService
	{
		User[] getUsers()
		{
			throw new UnsupportedOperationException();
		}


		void save(User aUser)
		{
			throw new UnsupportedOperationException();
		}
	}


	static class User
	{
		String mName;
		String mDescription;


		public User(String aName, String aDescription)
		{
			mName = aName;
			mDescription = aDescription;
		}


		@Override
		public String toString()
		{
			return mName;
		}
	}
}
