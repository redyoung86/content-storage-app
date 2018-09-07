package cc.ayakurayuki.contentstorage.module.settings.service

import cc.ayakurayuki.contentstorage.common.base.BaseBean
import cc.ayakurayuki.contentstorage.common.util.GoogleAuthenticator
import cc.ayakurayuki.contentstorage.common.util.IDUtils
import cc.ayakurayuki.contentstorage.common.util.RandomUtils
import cc.ayakurayuki.contentstorage.module.settings.dao.SettingsDAO
import cc.ayakurayuki.contentstorage.module.settings.entity.EmergencyKey
import cc.ayakurayuki.contentstorage.module.settings.entity.Settings
import com.alibaba.fastjson.JSON
import com.google.common.collect.Lists
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by Ayakura Yuki on 2017/10/19.
 */
@Service("SettingsService")
@Transactional(readOnly = true)
class SettingsService extends BaseBean {

  @Autowired
  SettingsDAO settingsDAO

  Settings get(String id) {
    settingsDAO.get(id)
  }

  private def insert(Settings settings) {
    settingsDAO.insert(settings)
  }

  def update(Settings settings) {
    settingsDAO.update(settings)
  }

  def delete(Settings settings) {
    settingsDAO.delete(settings)
  }

  /**
   * 从数据库获取包含Google Authenticator特征码的Settings对象。
   * @return 数据库中存在的对象，不存在则返回null。
   */
  private Settings getSecretSetting() {
    settingsDAO.getByKey(SECRET)
  }

  /**
   * 获取应急码Settings对象
   * @return
   */
  private Settings getEmergencySetting() {
    settingsDAO.getByKey(EMERGENCY)
  }

  /**
   * 获取特征码
   * @return 特征码字符串
   */
  private String getSecretKeyFromDatabase() {
    def settings = secretSetting
    if (null == settings) {
      settings = new Settings()
      settings.id = IDUtils.UUID()
      settings.key = SECRET
      settings.value = GoogleAuthenticator.generateSecretKey()
      insert(settings)
    } else {
      settings.value = GoogleAuthenticator.generateSecretKey()
      update(settings)
    }
    return settings.value
  }

  def validateAuthCode(def authCode) {
    def code = Long.valueOf(authCode as String)
    def googleAuthenticator = new GoogleAuthenticator()
    googleAuthenticator.windowSize = 5
    def settings = secretSetting
    return googleAuthenticator.checkCode(settings.value, code)
  }

  /**
   * 获取应急码
   * @return
   */
  List<EmergencyKey> generateEmergencyCode() {
    List<EmergencyKey> list = Lists.newArrayList()
    for (i in 1..10) {
      def key = new EmergencyKey()
      key.key = RandomUtils.emergencyKey
      key.effective = true
      list.add(key)
    }
    def settings = emergencySetting
    if (null == settings) {
      settings = new Settings()
      settings.id = IDUtils.UUID()
      settings.key = EMERGENCY
      settings.value = JSON.toJSONString(list)
      insert(settings)
    } else {
      settings.value = JSON.toJSONString(list)
      update(settings)
    }
    return list
  }

  String getQRCode(String conditionCode) {
    GoogleAuthenticator.getQRCode(conditionCode, secretKeyFromDatabase)
  }

}