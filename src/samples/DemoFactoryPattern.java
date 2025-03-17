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

			// normal running
//			factory.documentService = caller -> new DocumentService(factory);

			// when developing & testing
			factory.documentService = caller -> new MockDocumentService(
				new Document("Document A", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
				new Document("Document B", "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.")
			);

			factory.background = caller -> Color.RED;
			factory.foreground = caller -> Color.BLUE;
			factory.style = caller -> new Style(factory);
			factory.textSize = caller -> caller instanceof Style ? 48f : 24f;
			factory.documentPanel = caller -> new DocumentPanel(factory);

			DocumentPanel panel = factory.documentPanel.apply(null);

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
		Function<Object, DocumentService> documentService;
		Function<Object, Color> background;
		Function<Object, Color> foreground;
		Function<Object, Style> style;
		Function<Object, Float> textSize;
		Function<Object, DocumentPanel> documentPanel;
	}


	static class Style
	{
		private Color mText;
		private Color mBackground;
		private float mTextSize = 12f;


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


	static class DocumentPanel extends JPanel
	{
		private DocumentService mDocumentService;
		private Style mStyle;
		private float mTextSize = 12f;
		private Document mDocument;


		public DocumentPanel(Factory aFactory)
		{
			mDocumentService = aFactory.documentService.apply(this);
			mStyle = aFactory.style.apply(this);
			mTextSize = aFactory.textSize.apply(this);

			buildForm();
		}


		public void buildForm()
		{
			JList<Document> list = new JList<>(mDocumentService.getDocument());
			list.setFont(list.getFont().deriveFont(mTextSize));

			JTextArea text = new JTextArea();
			text.setForeground(mStyle.mText);
			text.setBackground(mStyle.mBackground);
			text.setFont(text.getFont().deriveFont(mStyle.mTextSize));
			text.setLineWrap(true);

			list.addListSelectionListener(aEvent ->
			{
				if (mDocument != null && !mDocument.mDescription.equals(text.getText()))
				{
					mDocument.mDescription = text.getText();
					mDocumentService.save(mDocument);
				}

				mDocument = list.getModel().getElementAt(list.getSelectedIndex());

				text.setText(mDocument.mDescription);
			});

			super.setLayout(new BorderLayout());
			super.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(list), new JScrollPane(text)));
		}
	}


	static class MockDocumentService extends DocumentService
	{
		Document[] mDocuments;


		public MockDocumentService(Document... aDocuments)
		{
			mDocuments = aDocuments;
		}


		@Override
		public Document[] getDocument()
		{
			return mDocuments;
		}


		@Override
		public void save(Document aDocument)
		{
			System.out.println("saved document: " + aDocument + "=" + aDocument.mDescription);
		}
	}


	static class DocumentService
	{
		private DocumentService()
		{
		}


		public DocumentService(Factory aFactory)
		{
		}


		Document[] getDocument()
		{
			throw new UnsupportedOperationException();
		}


		void save(Document aDocument)
		{
			throw new UnsupportedOperationException();
		}
	}


	static class Document
	{
		String mName;
		String mDescription;


		public Document(String aName, String aDescription)
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
