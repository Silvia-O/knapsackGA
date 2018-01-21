package knapsackGA;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Frame extends JFrame {

	private JPanel contentPane;
	private JTextField tfCapacity;
	private JTextField tfScale;
	private JTextField tfMaxgen;
	private JTextArea taResult = new JTextArea("");
	private JScrollPane spResult;
	private JTextArea taBest = new JTextArea("");
	private JScrollPane spBest;
	private JButton btnRun;

	

	private float[] weight = null; 
	private float[] profit = null; 
	private int len = 0; 
	private float capacity = 0; 
	private int scale = 0; 
	private int maxgen = 0;
	private float irate = 0.5f; 
	private float arate1 = 0.05f; 
	private float arate2 = 0.1f; 
	private File data = new File(".//data.txt"); 
	private Random random = new Random(System.currentTimeMillis()); 
	
	private boolean[][] population = null; 
	private float[] fitness = null; 
	private float bestFitness = 0; 
	private boolean[] bestUnit = null; 

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame frame = new Frame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// read goods' data 
	private void readData() {
		List<Object> tmp = Reader.read(data);
		weight = (float[]) tmp.get(0);
		profit = (float[]) tmp.get(1);
		len = weight.length;
	}

	// initialize the population
	private void initPopulation() {
		bestFitness = 0;     // reset
		fitness = new float[scale];
		population = new boolean[scale][len];
		// for each individual, get a random capacity(0.5 capacity to 1.5 capacity)
		// then choose random goods into this individual until the total weight is over the capacity
		for (int i = 0; i < scale; i++) {
			float tmp = (float) (0.5 + Math.random()) * capacity;
			int count = 0; // prevent consuming too much computing resource
			for (int j = 0; j < tmp;) {
				int k = random.nextInt(len);
				if (population[i][k]) {
					if (count == 3) {
						break;
					}
					count++;
					continue;
				} else {
					population[i][k] = true;
					j += weight[k];
					count = 0;
				}
			}
		}
	}

	// calculate each individual's fitness
	private float evaluate(boolean[] unit) {
		float profitSum = 0;
		float weightSum = 0;
		for (int i = 0; i < unit.length; i++) {
			if (unit[i]) {
				weightSum += weight[i];
				profitSum += profit[i];
			}
		}
		if (weightSum > capacity) {
			// this individual's total weight is over the capacity of the knapsack
			return 0;
		} else {
			return profitSum;
		}
	}

	// calculate individuals' fitness in the population
	private void calcFitness() {
		for (int i = 0; i < scale; i++) {
			fitness[i] = evaluate(population[i]);
		}
	}

	// record the best fit
	private void recBest(int gen) {
		for (int i = 0; i < scale; i++) {
			if (fitness[i] > bestFitness) {
				bestFitness = fitness[i];
				bestUnit = new boolean[len];
				for (int j = 0; j < len; j++) {
					bestUnit[j] = population[i][j];
				}
			}
		}
	}

	// select
	private void select() {
		// calculate each individual's select rate
		float sum = 0;
		double[] select_rate = new double[scale];
		for (int i = 0; i < scale; i++) {
			sum += fitness[i];
		}
		for (int i = 0; i < scale; i++) {
			select_rate[i] = fitness[i] / sum;
		}
		// calculate each individual's accumulated select rate
		double[] accu_rate = new double[scale];
		for (int i = 0; i < scale; i++) {
			if (i == 0) {
				accu_rate[i] = select_rate[i];
			} else {
				accu_rate[i] = accu_rate[i - 1] + select_rate[i];
			}
		}
		// roulette
		boolean[][] tmpPopulation = new boolean[scale][len];
		for (int i = 0; i < scale; i++) {
			double r = Math.random();
			if (r <= accu_rate[0]) {
				tmpPopulation[i] = population[0];
			} else {
				for (int j = 1; j < scale; j++) {
					if (r < accu_rate[j]) {
						tmpPopulation[i] = population[j];
						break;
					}
				}
			}
		}
		population = tmpPopulation;
	}

	// intersect
	private void intersect() {
		for (int i = 0; i < scale; i = i + 2)
			for (int j = 0; j < len; j++) {
				if (Math.random() < irate) { 
					boolean tmp = population[i][j];
					population[i][j] = population[(i + 1) % scale][j]; 
					population[(i + 1) % scale][j] = tmp;
				}
			}
	}

	// aberrance
	private void aberra() {
		for (int i = 0; i < scale; i++) {
			if (Math.random() > arate1) {
				continue;
			}
			for (int j = 0; j < len; j++) {
				if (Math.random() < arate2) {
					population[i][j] = !population[i][j];
				}
			}
		}
	}

	/**
	 * Create the frame.
	 */
	public Frame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 655, 431);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setTitle("solution to 0-1 knapsack problem by GA");
		setVisible(true);

		JLabel lblCapacity = new JLabel("capacity:");
		lblCapacity.setBounds(14, 55, 85, 18);
		contentPane.add(lblCapacity);

		tfCapacity = new JTextField("10");
		tfCapacity.setBounds(94, 52, 86, 24);
		contentPane.add(tfCapacity);
		tfCapacity.setColumns(10);

		JLabel lblMaxgen = new JLabel("maxgen:");
		lblMaxgen.setBounds(14, 150, 75, 18);
		contentPane.add(lblMaxgen);

		tfMaxgen = new JTextField("100");
		tfMaxgen.setBounds(94, 147, 86, 24);
		contentPane.add(tfMaxgen);
		tfMaxgen.setColumns(10);

		JLabel lblScale = new JLabel("scale:");
		lblScale.setBounds(14, 102, 82, 18);
		contentPane.add(lblScale);

		tfScale = new JTextField("20");
		tfScale.setBounds(94, 99, 86, 24);
		contentPane.add(tfScale);
		tfScale.setColumns(10);

		JLabel lblResult = new JLabel("run results");
		lblResult.setBounds(209, 55, 350, 18);
		contentPane.add(lblResult);

		JScrollPane spResult = new JScrollPane(taResult);
		spResult.setBounds(209, 86, 354, 154);
		contentPane.add(spResult);

		JLabel lblBest = new JLabel("optimal way");
		lblBest.setBounds(209, 260, 159, 18);
		contentPane.add(lblBest);

		JScrollPane spBest = new JScrollPane(taBest);
		spBest.setBounds(209, 286, 354, 52);
		contentPane.add(spBest);

		JButton btnRun = new JButton("Run");
		btnRun.setBounds(42, 297, 113, 27);
		contentPane.add(btnRun);
		
		String descStr = "<html><font color=black>notice:<br/>the goods' data be set in the file'data.txt'<font/>";  
		JLabel lblDesc = new JLabel(descStr);
		lblDesc.setBounds(13, 182, 167, 119);
		contentPane.add(lblDesc);
		

		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					solve();
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	
	/*
	 * run
	 */
	public void solve() throws CloneNotSupportedException {
		taResult.setText(""); // clear
		taBest.setText("");
		String capacityStr = tfCapacity.getText().replaceAll("[^\\d]", "");// remove all non-numeric chars
		String scaleStr = tfScale.getText().replaceAll("[^\\d]", "");
		String maxgenStr = tfMaxgen.getText().replaceAll("[^\\d]", "");
		StringBuffer sb = new StringBuffer();

		if (!capacityStr.equals("") && !maxgenStr.equals("")) { // not "";
			capacity = Integer.parseInt(capacityStr);    // convert to numbers
			scale = Integer.parseInt(scaleStr);
			maxgen = Integer.parseInt(maxgenStr);
		}

		// GA
		readData();
		initPopulation();
		for (int i = 0; i < maxgen; i++) {
			calcFitness();
			recBest(i);
			select();
			intersect();
			aberra();

			/*
			 * show the results
			 */
			int totalProfit = 0;
			int totalWeight = 0;

			sb.append((i + 1) + ":\n" + "order   weight   profit\n");
			for (int j = 0; j < bestUnit.length; j++) {
				if (bestUnit[j]) {
					totalProfit += profit[j];
					totalWeight += weight[j];
					sb.append("  " + (j + 1) + ".               " + weight[j] + "      " + profit[j] + "\n");
					if (i == maxgen - 1) {
						taBest.append("  " + (j + 1) + ".               " + weight[j] + "      " + profit[j] + "\n");
					}
				}
			}
			sb.append("total profit:  " + totalProfit + "\n");
			sb.append("total weight:  " + totalWeight + "\n");
			if (i == maxgen - 1) {
				taBest.append("total profit:" + totalProfit + "\n");
				taBest.append("total weight:" + totalWeight + "\n");
			}
		}
		taResult.setText(sb.toString());
		taResult.setCaretPosition(0); // top the cursor
		taBest.setCaretPosition(0);
	}
}
