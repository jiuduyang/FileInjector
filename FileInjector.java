import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class FileInjector extends JFrame {
    // 全局变量
    private JTextField sourceFileField;
    private JTextField targetDirField;
    private JTextField injectCountField;
    private JTextField prefixField;
    private JTextField suffixField;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JButton startButton;
    private JButton stopButton;
    private JButton resetButton;
    private JButton openTargetButton;
    private JButton openLogButton;
    private JButton modeButton;
    
    private File sourceFile;
    private File targetDir;
    private int injectCount = 10;
    private String prefix = "";
    private String suffix = "";
    private boolean isDarkMode = false;
    private boolean isRunning = false;
    private boolean stopRequested = false;
    
    // 颜色定义
    private final Color LIGHT_BACKGROUND = Color.WHITE;
    private final Color LIGHT_FOREGROUND = Color.BLACK;
    private final Color DARK_BACKGROUND = new Color(51, 51, 51);
    private final Color DARK_FOREGROUND = Color.WHITE;
    
    public FileInjector() {
        // 设置窗口属性
        setTitle("文件随机注入隐藏工具");
        setSize(580, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(LIGHT_BACKGROUND);
        add(mainPanel);
        
        // 源文件选择
        JLabel sourceLabel = new JLabel("源文件：");
        sourceLabel.setBounds(10, 10, 80, 20);
        sourceLabel.setForeground(LIGHT_FOREGROUND);
        mainPanel.add(sourceLabel);
        
        sourceFileField = new JTextField();
        sourceFileField.setBounds(100, 10, 300, 20);
        sourceFileField.setEditable(false);
        mainPanel.add(sourceFileField);
        
        JButton sourceButton = new JButton("浏览...");
        sourceButton.setBounds(410, 10, 80, 20);
        sourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectSourceFile();
            }
        });
        mainPanel.add(sourceButton);
        
        // 目标目录选择
        JLabel targetLabel = new JLabel("目标目录：");
        targetLabel.setBounds(10, 40, 80, 20);
        targetLabel.setForeground(LIGHT_FOREGROUND);
        mainPanel.add(targetLabel);
        
        targetDirField = new JTextField();
        targetDirField.setBounds(100, 40, 300, 20);
        targetDirField.setEditable(false);
        mainPanel.add(targetDirField);
        
        JButton targetButton = new JButton("浏览...");
        targetButton.setBounds(410, 40, 80, 20);
        targetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectTargetDir();
            }
        });
        mainPanel.add(targetButton);
        
        // 注入数量
        JLabel countLabel = new JLabel("注入数量：");
        countLabel.setBounds(10, 70, 80, 20);
        countLabel.setForeground(LIGHT_FOREGROUND);
        mainPanel.add(countLabel);
        
        injectCountField = new JTextField(String.valueOf(injectCount));
        injectCountField.setBounds(100, 70, 100, 20);
        mainPanel.add(injectCountField);
        
        // 重命名规则
        JLabel prefixLabel = new JLabel("文件名前缀：");
        prefixLabel.setBounds(10, 100, 80, 20);
        prefixLabel.setForeground(LIGHT_FOREGROUND);
        mainPanel.add(prefixLabel);
        
        prefixField = new JTextField(prefix);
        prefixField.setBounds(100, 100, 100, 20);
        mainPanel.add(prefixField);
        
        JLabel suffixLabel = new JLabel("文件名后缀：");
        suffixLabel.setBounds(210, 100, 80, 20);
        suffixLabel.setForeground(LIGHT_FOREGROUND);
        mainPanel.add(suffixLabel);
        
        suffixField = new JTextField(suffix);
        suffixField.setBounds(300, 100, 100, 20);
        mainPanel.add(suffixField);
        
        // 操作按钮
        startButton = new JButton("开始注入");
        startButton.setBounds(10, 130, 100, 30);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startInject();
            }
        });
        mainPanel.add(startButton);
        
        stopButton = new JButton("停止");
        stopButton.setBounds(120, 130, 100, 30);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopInject();
            }
        });
        mainPanel.add(stopButton);
        
        resetButton = new JButton("重置");
        resetButton.setBounds(230, 130, 100, 30);
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });
        mainPanel.add(resetButton);
        
        openTargetButton = new JButton("打开目标目录");
        openTargetButton.setBounds(340, 130, 100, 30);
        openTargetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openTargetDir();
            }
        });
        mainPanel.add(openTargetButton);
        
        openLogButton = new JButton("打开日志目录");
        openLogButton.setBounds(450, 130, 100, 30);
        openLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLogDir();
            }
        });
        mainPanel.add(openLogButton);
        
        // 进度条
        progressBar = new JProgressBar();
        progressBar.setBounds(10, 170, 540, 20);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        mainPanel.add(progressBar);
        
        progressLabel = new JLabel("准备就绪");
        progressLabel.setBounds(10, 190, 540, 20);
        progressLabel.setForeground(LIGHT_FOREGROUND);
        mainPanel.add(progressLabel);
        
        // 模式切换
        modeButton = new JButton("深色模式");
        modeButton.setBounds(490, 10, 80, 20);
        modeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleMode();
            }
        });
        mainPanel.add(modeButton);
    }
    
    // 选择源文件
    private void selectSourceFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            sourceFile = fileChooser.getSelectedFile();
            sourceFileField.setText(sourceFile.getAbsolutePath());
        }
    }
    
    // 选择目标目录
    private void selectTargetDir() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            targetDir = fileChooser.getSelectedFile();
            targetDirField.setText(targetDir.getAbsolutePath());
        }
    }
    
    // 开始注入
    private void startInject() {
        // 验证输入
        if (sourceFile == null) {
            JOptionPane.showMessageDialog(this, "请选择源文件！");
            return;
        }
        if (targetDir == null) {
            JOptionPane.showMessageDialog(this, "请选择目标目录！");
            return;
        }
        
        try {
            injectCount = Integer.parseInt(injectCountField.getText());
            if (injectCount <= 0) {
                JOptionPane.showMessageDialog(this, "请输入有效的注入数量！");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的注入数量！");
            return;
        }
        
        prefix = prefixField.getText();
        suffix = suffixField.getText();
        
        // 扫描目标目录下的所有子文件夹
        java.util.List<File> folders = new ArrayList<>();
        scanFolders(targetDir, folders);
        
        if (folders.size() < injectCount) {
            JOptionPane.showMessageDialog(this, "目标目录下的子文件夹数量不足，无法完成注入！");
            return;
        }
        
        // 随机打乱文件夹顺序
        Collections.shuffle(folders);
        
        // 生成日志文件路径
        File locationsLog = new File(targetDir, "文件隐藏位置日志.txt");
        File runLog = new File(targetDir, "程序运行日志.txt");
        
        // 清空旧日志
        try {
            Files.deleteIfExists(locationsLog.toPath());
            Files.deleteIfExists(runLog.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        
        // 开始注入
        isRunning = true;
        stopRequested = false;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        
        new Thread(() -> {
            int successCount = 0;
            int failedCount = 0;
            
            for (int i = 0; i < injectCount && !stopRequested; i++) {
                File folder = folders.get(i);
                
                // 生成随机文件名
                String randomName = generateRandomName();
                String fileExt = getFileExtension(sourceFile);
                String newFileName = prefix + randomName + suffix + (fileExt.isEmpty() ? "" : "." + fileExt);
                File newFile = new File(folder, newFileName);
                
                // 复制文件
                try {
                    Files.copy(sourceFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    successCount++;
                    
                    // 记录到位置日志
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(locationsLog, true))) {
                        writer.write(newFile.getAbsolutePath());
                        writer.newLine();
                    }
                } catch (IOException e) {
                    failedCount++;
                }
                
                // 更新进度
                final int currentProgress = (i + 1) * 100 / injectCount;
                final int currentIndex = i + 1;
                final int finalInjectCount = injectCount;
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(currentProgress);
                    progressLabel.setText("正在注入：" + currentIndex + "/" + finalInjectCount);
                });
                
                // 短暂延迟，避免界面卡死
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // 记录结束时间
            LocalDateTime endTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            // 写入运行日志
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(runLog))) {
                writer.write("执行时间：" + startTime.format(formatter) + " - " + endTime.format(formatter));
                writer.newLine();
                writer.write("注入成功数量：" + successCount);
                writer.newLine();
                writer.write("注入失败数量：" + failedCount);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // 更新界面
            final int finalSuccessCount = successCount;
            final int finalFailedCount = failedCount;
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(100);
                progressLabel.setText("注入完成！成功：" + finalSuccessCount + "，失败：" + finalFailedCount);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                isRunning = false;
                
                // 提示完成
                JOptionPane.showMessageDialog(FileInjector.this, "注入完成！\n成功：" + finalSuccessCount + "\n失败：" + finalFailedCount);
            });
        }).start();
    }
    
    // 停止注入
    private void stopInject() {
        if (isRunning) {
            stopRequested = true;
            progressLabel.setText("正在停止...");
        }
    }
    
    // 重置表单
    private void resetForm() {
        sourceFile = null;
        targetDir = null;
        injectCount = 10;
        prefix = "";
        suffix = "";
        
        sourceFileField.setText("");
        targetDirField.setText("");
        injectCountField.setText(String.valueOf(injectCount));
        prefixField.setText(prefix);
        suffixField.setText(suffix);
        
        progressBar.setValue(0);
        progressLabel.setText("准备就绪");
    }
    
    // 打开目标目录
    private void openTargetDir() {
        if (targetDir != null) {
            try {
                Desktop.getDesktop().open(targetDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择目标目录！");
        }
    }
    
    // 打开日志目录
    private void openLogDir() {
        if (targetDir != null) {
            try {
                Desktop.getDesktop().open(targetDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择目标目录！");
        }
    }
    
    // 切换模式
    private void toggleMode() {
        isDarkMode = !isDarkMode;
        Component[] components = getContentPane().getComponents();
        
        if (isDarkMode) {
            // 深色模式
            getContentPane().setBackground(DARK_BACKGROUND);
            modeButton.setText("浅色模式");
            
            for (Component component : components) {
                if (component instanceof JPanel) {
                    JPanel panel = (JPanel) component;
                    panel.setBackground(DARK_BACKGROUND);
                    
                    Component[] panelComponents = panel.getComponents();
                    for (Component panelComponent : panelComponents) {
                        if (panelComponent instanceof JLabel) {
                            JLabel label = (JLabel) panelComponent;
                            label.setForeground(DARK_FOREGROUND);
                        } else if (panelComponent instanceof JButton) {
                            JButton button = (JButton) panelComponent;
                            button.setBackground(new Color(60, 60, 60));
                            button.setForeground(DARK_FOREGROUND);
                        } else if (panelComponent instanceof JTextField) {
                            JTextField textField = (JTextField) panelComponent;
                            textField.setBackground(new Color(60, 60, 60));
                            textField.setForeground(DARK_FOREGROUND);
                        } else if (panelComponent instanceof JProgressBar) {
                            JProgressBar progressBar = (JProgressBar) panelComponent;
                            progressBar.setForeground(new Color(0, 122, 204));
                        }
                    }
                }
            }
        } else {
            // 浅色模式
            getContentPane().setBackground(LIGHT_BACKGROUND);
            modeButton.setText("深色模式");
            
            for (Component component : components) {
                if (component instanceof JPanel) {
                    JPanel panel = (JPanel) component;
                    panel.setBackground(LIGHT_BACKGROUND);
                    
                    Component[] panelComponents = panel.getComponents();
                    for (Component panelComponent : panelComponents) {
                        if (panelComponent instanceof JLabel) {
                            JLabel label = (JLabel) panelComponent;
                            label.setForeground(LIGHT_FOREGROUND);
                        } else if (panelComponent instanceof JButton) {
                            JButton button = (JButton) panelComponent;
                            button.setBackground(UIManager.getColor("Button.background"));
                            button.setForeground(UIManager.getColor("Button.foreground"));
                        } else if (panelComponent instanceof JTextField) {
                            JTextField textField = (JTextField) panelComponent;
                            textField.setBackground(UIManager.getColor("TextField.background"));
                            textField.setForeground(UIManager.getColor("TextField.foreground"));
                        } else if (panelComponent instanceof JProgressBar) {
                            JProgressBar progressBar = (JProgressBar) panelComponent;
                            progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
                        }
                    }
                }
            }
        }
    }
    
    // 扫描文件夹
    private void scanFolders(File directory, java.util.List<File> folders) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && !file.isHidden() && !isSystemDirectory(file)) {
                    folders.add(file);
                    scanFolders(file, folders);
                }
            }
        }
    }
    
    // 判断是否为系统目录
    private boolean isSystemDirectory(File directory) {
        String name = directory.getName().toLowerCase();
        return name.equals("system volume information") || name.equals("recycler") || name.equals("$recycle.bin");
    }
    
    // 生成随机文件名
    private String generateRandomName() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }
    
    // 获取文件扩展名
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1);
    }
    
    // 主方法
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileInjector injector = new FileInjector();
            injector.setVisible(true);
        });
    }
}