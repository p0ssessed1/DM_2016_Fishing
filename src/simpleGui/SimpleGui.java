package simpleGui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.filter.ItemListFilter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;

import banking.Banking;
import fishing.FishAction;
import fishing.Fishing;

public class SimpleGui implements ActionListener {

	final Area DRAYNOR = new Area(new Position(3092, 3245, 0), new Position(3093, 3242, 0));
	final Area EAST_FALLY = new Area(new Position(3011, 3355, 0), new Position(3014, 3357, 0));
	final Area WEST_FALLY = new Area(new Position(2945, 3368, 0), new Position(2946, 3371, 0));
	final Area EAST_VARROCK = new Area(new Position(3251, 3240, 0), new Position(3254, 3442, 0));
	final Area WEST_VARROCK = new Area(new Position(3185, 3436, 0), new Position(3182, 3444, 0));
	final Area LUMBRIDGE = new Area(new Position(3208, 3220, 2), new Position(3209, 3218, 2));
	final Area EDGEVILLE = new Area(new Position(3094, 3489, 0), new Position(3092, 3495, 0));
	final Area[] BANKS = { DRAYNOR, EAST_FALLY, WEST_FALLY, EAST_VARROCK, WEST_VARROCK, LUMBRIDGE, EDGEVILLE };
	final List<String> BANK_NAMES = new LinkedList<String>(Arrays.asList("Draynor", "East Falador", "West Falador",
			"East Varrock", "West Varrock", "Lumbridge", "Edgeville"));
	final String[] fishing_actions = { "Cage", "Net", "Lure", "Bait", "Harpoon" };
	final ActionFilter<NPC> af = new ActionFilter(fishing_actions);

	List<String> bank_names_list = new LinkedList<String>();
	Script script;
	boolean setUp = false;
	boolean start = false;
	Banking bank;
	Fishing fish;

	JFrame frame = new JFrame("Dynamic Fisher");
	NameFilter<Item> keepItems;
	List<JRadioButton> fishingOptions = new LinkedList<JRadioButton>();
	List<JRadioButton> banks = new LinkedList<JRadioButton>();
	List<JCheckBox> keep = new LinkedList<JCheckBox>();
	ButtonGroup bg_f = new ButtonGroup();
	ButtonGroup bg_b = new ButtonGroup();
	ActionFilter<NPC> f;
	/*
	 * TODO - implement different types of fish, since one action can mean
	 * multiple different fish types.
	 */
	JPanel panel = new JPanel(new GridLayout(0, 1));

	public SimpleGui(Script script, Fishing f, Banking b) {
		this.script = script;
		this.fish = f;
		this.bank = b;
	}

	/**
	 * Sets up a simple Gui with start button.
	 * 
	 * @return Void
	 */
	public void Setup() {
		List<NPC> spots = script.getNpcs().getAll();
		List<FishAction> fa = new LinkedList<FishAction>();
		String[] spot_actions = new String[2];
		for (NPC n : spots) {
			if (n != null && af.match(n)) {
				int id = n.getId();
				if (id != -1) {
					for (NPC i : script.getNpcs().get(n.getX(), n.getY())) {
						if (i.getId() == id) {
							spot_actions = n.getActions();
							if (!fa.contains(spot_actions[0]) || !fa.contains(spot_actions[1])) {
								script.log("Got here. fish.getFishFromSpot = " + fish.getFishFromSpot(i));
								if (!fa.contains(fish.getFishFromSpot(i))) {
									fa.addAll(fish.getFishFromSpot(i));
								}

							}
						}
					}
				}
			}
		}
		JPanel fishingPanel = new JPanel(new GridLayout(0, 3));
		fishingPanel.add(new Label("Type of Fish:"));
		fishingPanel.add(new Label("     "));
		JPanel bankPanel = new JPanel(new GridLayout(0, 3));
		bankPanel.add(new Label("Banks: "));
		bankPanel.add(new Label("Drop if none"));
		JPanel optionPanel = new JPanel(new GridLayout(0, 2));
		optionPanel.add(new Label("Keep Items:"));
		List<String> fishNames = new LinkedList<String>();
		for (FishAction f : fa) {
			if (!fishNames.contains(f.fish)) {
				fishNames.add(f.fish);
			}
		}

		for (String s: fishNames) {
			fishingOptions.add(new JRadioButton(s));
		}
		for (JRadioButton b : fishingOptions) {
			bg_f.add(b);
			fishingPanel.add(b);
		}

		for (String s : BANK_NAMES) {
			banks.add(new JRadioButton(s));
		}

		for (JRadioButton b : banks) {
			bg_b.add(b);
			bankPanel.add(b);
		}

		Item[] inv = script.getInventory().getItems();
		List<String> exclusiveInv = new LinkedList<String>();
		for(Item i: inv){
			if(!exclusiveInv.contains(i.getName())){
				exclusiveInv.add(i.getName());
			}
		}
		
		for (String i : exclusiveInv) {
			if (i != null) {
				keep.add(new JCheckBox(i));
			}
		}

		for (JCheckBox b : keep) {
			optionPanel.add(b);
		}

		JButton start = new JButton("Start");
		start.addActionListener(this);
		panel.add(fishingPanel, BorderLayout.NORTH);
		panel.add(new JSeparator());
		panel.add(bankPanel);
		panel.add(new JSeparator());
		panel.add(optionPanel);
		panel.add(new JSeparator());
		panel.add(start);
		frame.add(panel);
		frame.pack();
		setUp = true;
	}

	/**
	 * Displays gui set up previously
	 * 
	 * @return True if display successful\n False if not set up.
	 * @throws InterruptedException
	 */
	public boolean Display() throws InterruptedException {
		if (setUp) {
			frame.setVisible(true);
			while (!start) {
				Script.sleep(100);
			}
			frame.dispose();
		}
		return setUp;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		try {
			String[] to_keep = new String[keep.size()];
			JCheckBox local;
			for (int i = 0; i < keep.size(); i++) {
				local = keep.get(i);
				if (local.isSelected()) {
					to_keep[i] = local.getText();
				}
			}
			keepItems = new NameFilter<Item>(to_keep);
			bank.setKeepItems(keepItems);
			String selectedBank = null;
			for (JRadioButton b : banks) {
				if (b.isSelected()) {
					selectedBank = b.getText();
				}
			}
			String selectedFish = null;
			for (JRadioButton f : fishingOptions) {
				if (f.isSelected()) {
					selectedFish = f.getText();
				}
			}
			bank.setArea(BANKS[BANK_NAMES.indexOf(selectedBank)]);
			fish.setFish(selectedFish);
			script.log("Changing start.");
			start = true;
		} catch (Exception e) {
			script.log("Exception in actionPerformed." + e);
		}
	}

}
