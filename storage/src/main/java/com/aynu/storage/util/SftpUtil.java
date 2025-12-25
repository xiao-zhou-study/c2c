package com.aynu.storage.util;

import com.aynu.common.exceptions.BadRequestException;
import com.aynu.storage.config.FileUploadProperties;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Properties;

/**
 * SFTP工具类 - 用于上传文件到云服务器
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SftpUtil {

    private final FileUploadProperties uploadProperties;

    /**
     * 上传文件到云服务器
     *
     * @param inputStream 文件输入流
     * @param remotePath  远程目录路径（相对于basePath）
     * @param fileName    文件名
     */
    public String uploadFile(InputStream inputStream, String remotePath, String fileName) {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            // 1. 创建JSch对象
            JSch jsch = new JSch();

            // 2. 获取配置
            FileUploadProperties.Sftp sftpConfig = uploadProperties.getSftp();

            // 3. 创建Session
            session = jsch.getSession(sftpConfig.getUsername(), sftpConfig.getHost(), sftpConfig.getPort());
            session.setPassword(sftpConfig.getPassword());

            // 4. 设置不验证主机密钥
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // 5. 设置超时并连接
            session.setTimeout(sftpConfig.getTimeout());
            session.connect();

            // 6. 打开SFTP通道
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // 7. 构建完整远程路径
            String fullRemotePath = uploadProperties.getBasePath() + "/" + remotePath;

            // 8. 创建远程目录（递归创建）
            createRemoteDirectory(channelSftp, fullRemotePath);

            // 9. 切换到目标目录
            channelSftp.cd(fullRemotePath);

            // 10. 上传文件
            channelSftp.put(inputStream, fileName);

            log.info("文件上传成功: {}/{}", fullRemotePath, fileName);
            return uploadProperties.getUrlPrefix() + "/" + remotePath + "/" + fileName;

        } catch (JSchException | SftpException e) {
            log.error("SFTP上传文件失败", e);
            throw new BadRequestException("文件上传到云服务器失败: " + e.getMessage());
        } finally {
            // 11. 关闭连接
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 递归创建远程目录
     */
    private void createRemoteDirectory(ChannelSftp channelSftp, String remotePath) throws SftpException {
        String[] folders = remotePath.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String folder : folders) {
            if (folder.isEmpty()) {
                currentPath.append("/");
                continue;
            }
            currentPath.append(folder).append("/");
            try {
                channelSftp.cd(currentPath.toString());
            } catch (SftpException e) {
                // 目录不存在，创建它
                try {
                    channelSftp.mkdir(currentPath.toString());
                    channelSftp.cd(currentPath.toString());
                } catch (SftpException ex) {
                    // 可能是权限问题或其他错误
                    log.warn("创建目录失败: {}", currentPath);
                }
            }
        }
    }

}
