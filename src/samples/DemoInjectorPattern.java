package samples;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import org.terifan.injector.Inject;
import org.terifan.injector.Injector;
import org.terifan.injector.Named;
import org.terifan.injector.PostConstruct;
import org.terifan.injector.Provider;


public class DemoInjectorPattern
{
	public static void main(String... args)
	{
		try
		{
			Injector injector = new Injector();

			injector.setLog(System.out);

			// normal running
//			injector.bind(UserService.class).asSingleton();

			// when developing & testing
			injector.bind(UserService.class).toInstance(new MockUserService(
				new User("Dave", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
				new User("Steve", "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.")
			));

			injector.bind(Color.class).named("background").toInstance(Color.RED);
			injector.bind(Color.class).named("foreground").toProvider(()->Color.BLUE);

			injector.bindConstant().named("textSize").to(24f);
			injector.bindConstant().named("textSize").in(Style.class).to(48f);

			// replace style
//			injector.bind(Style.class).toInstance(new Style(Color.RED, Color.BLUE));

			UserPanel panel = injector.getInstance(UserPanel.class);

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


	static class Style
	{
		private Color mText;
		private Color mBackground;
		private float mTextSize = 12f;


		@Inject
		public Style(@Named("foreground") Color aText, @Named("background") Color aBackground, @Named("textSize") float aTextSize)
		{
			mText = aText;
			mBackground = aBackground;
			mTextSize =aTextSize;
		}


		public Style(Color aText, Color aBackground)
		{
			mText = aText;
			mBackground = aBackground;
		}
	}


	static class UserPanel extends JPanel
	{
		@Inject private Provider<UserService> mUserService;
		@Inject private Style mStyle;
		@Inject(optional = true) @Named("textSize") private float mTextSize = 12f;
		private User mUser;


		@PostConstruct
		public void buildForm()
		{
			JList<User> list = new JList<>(mUserService.get().getUsers());
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
					mUserService.get().save(mUser);
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
