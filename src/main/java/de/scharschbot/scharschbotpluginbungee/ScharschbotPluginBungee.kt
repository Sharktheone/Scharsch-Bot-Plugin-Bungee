package de.scharschbot.scharschbotpluginbungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import net.md_5.bungee.event.EventHandler
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.HttpClients
import java.io.File
import java.io.IOException


class ScharschbotPluginBungee : Plugin(), Listener {
    private var file: File? = null
    private var config: Configuration? = null
    private val configDirPath = ProxyServer.getInstance().pluginsFolder.toString() + "/ScharschBot-Bungee/"
    private val configPath = ProxyServer.getInstance().pluginsFolder.toString() + "/ScharschBot-Bungee/config.yml"

    override fun onEnable() {
        super.onEnable()

        logger.info("ScharschBot Bungee Plugin Loaded!")
        proxy.pluginManager.registerListener(this,this)
        try {
            val dir = File(configDirPath)
            if (!dir.exists()) dir.mkdir()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        file = File(configPath)
        try {
            if (!file!!.exists()) {
                file!!.createNewFile()
                config?.set("URL", "<URL to the scharsch-bot instance (including port)>")
                config?.set("User", "<username>")
                config?.set("Pass", "<password>")
                config?.set("ServerName", "<serverName>")
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(file)
            ConfigurationProvider.getProvider(YamlConfiguration::class.java).save(config, file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    override fun onDisable() {
        logger.info("ScharschBot Bungee Plugin Disabled - Bye see you next time")
    }
    private fun sendValues(Data: String){
        val httpClient = HttpClients.createDefault()
        logger.info(Data)
        try {
            val request = HttpPost(config?.getString("URL"))
            val creds = UsernamePasswordCredentials(config?.getString("User"),config?.getString("Pass"))

            request.entity = StringEntity(Data)
            request.setHeader("Content-type", "application/json")
            request.addHeader(BasicScheme().authenticate(creds, request, null))

            val response: CloseableHttpResponse = httpClient.execute(request)
            if ( !(response.statusLine.statusCode == 204 || response.statusLine.statusCode == 200) ) {
                logger.warning("Failure sending data to discord bot: " + response.statusLine.reasonPhrase)
            }
            response.close()
            httpClient.close()
        } catch (e: Exception) {
            logger.warning("Failed to send HTTP Request: " + e.message)
        }
    }

    @EventHandler
    fun playerJoin(event: PostLoginEvent){
        val joinJson = "{\"name\":\"" + event.player.name + "\", \"type\":\"join\", \"server\":\"" + config?.getString("ServerName") + "\"}"
        sendValues(joinJson)
    }
    @EventHandler
    fun playerQuit(event: PlayerDisconnectEvent){
        val quitJson = "{\"name\":\"" + event.player.name + "\", \"type\":\"quit\", \"server\":\"" + config?.getString("ServerName") + "\"}"
        sendValues(quitJson)
    }

}