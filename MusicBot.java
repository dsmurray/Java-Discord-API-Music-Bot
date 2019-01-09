package com.mycompany.MusicBot;

/*
 * Discord Music Bot
 * created by dsmurray
 */

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.core.entities.*;
import javax.security.auth.login.LoginException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;



public class MusicBot extends ListenerAdapter
{
    String[] LinksL = new String[5];
    String[] LinksY = new String[5];
    int s = 0;
    int y = 0;
    int s1 = 0;
    int y1 = 0;

    public static void main(String[] args)
    {
        try
        {
            JDA jda = new JDABuilder(AccountType.BOT)
                .setToken("____Y_O_U_R___T_O_K_E_N___H_E_R_E____") //The token of the account that is logging in.
                .addEventListener(new MusicBot()) //An instance of a class that will handle events.
                .buildBlocking();  //There are 2 ways to login, blocking vs async. Blocking guarantees that JDA will be completely loaded.
        }
        catch (LoginException e)
        {
            //If anything goes wrong in terms of authentication, this is the exception that will represent it
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            //Due to the fact that buildBlocking is a blocking method, one which waits until JDA is fully loaded,
            // the waiting can be interrupted. This is the exception that would fire in that situation.
            //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
            // you use buildBlocking in a thread that has the possibility of being interrupted (async thread usage and interrupts)
            e.printStackTrace();
        }
    }
    
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private MusicBot() 
    {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) 
    {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) 
        {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler((AudioPlayerSendHandler) musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //These are provided with every event in JDA
        JDA jda = event.getJDA();                       //JDA, the core of the api.
        long responseNumber = event.getResponseNumber();//The amount of discord events that JDA has received since the last reconnect.

        //Event specific information
        User author = event.getAuthor();                //The user that sent the message
        Message message = event.getMessage();           //The message that was received.
        MessageChannel channel = event.getChannel();    //This is the MessageChannel that the message was sent to.
        //  This could be a TextChannel, PrivateChannel, or Group!

        String msg = message.getContentDisplay();              //This returns a human readable version of the Message. Similar to
        // what you would see in the client.

        boolean bot = author.isBot();                    //This boolean is useful to determine if the User that
        // sent the Message is a BOT or not!
        
        if (bot == true) //do not read bot messages
        {
            
        }

        else if (event.isFromType(ChannelType.TEXT))         //If this message was sent to a Guild TextChannel
        {
            //Because we now know that this message was sent in a Guild, we can do guild specific things
            // Note, if you don't check the ChannelType before using these methods, they might return null due
            // the message possibly not being from a Guild!

            Guild guild = event.getGuild();             //The Guild that this message was sent in. (note, in the API, Guilds are Servers)
            TextChannel textChannel = event.getTextChannel(); //The TextChannel that this message was sent to.
            Member member = event.getMember();          //This Member that sent the message. Contains Guild specific information about the User!

            String name;
            if (message.isWebhookMessage())
            {
                name = author.getName();                //If this is a Webhook message, then there is no Member associated
            } // with the User, thus we default to the author for name.
            else
            {
                name = member.getEffectiveName();       //This will either use the Member's nickname if they have one,
            }                                           // otherwise it will default to their username. (User#getName())

            System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg);
        }
        
        //Enable for private channels and groups.
        
        /*else if (event.isFromType(ChannelType.PRIVATE)) //If this message was sent to a PrivateChannel
        {
            //The message was sent in a PrivateChannel.
            //In this example we don't directly use the privateChannel, however, be sure, there are uses for it!
            PrivateChannel privateChannel = event.getPrivateChannel();

            System.out.printf("[PRIV]<%s>: %s\n", author.getName(), msg);
        }
        
        else if (event.isFromType(ChannelType.GROUP))   //If this message was sent to a Group. This is CLIENT only!
        {
            //The message was sent in a Group. It should be noted that Groups are CLIENT only.
            Group group = event.getGroup();
            String groupName = group.getName() != null ? group.getName() : "";  //A group name can be null due to it being unnamed.

            System.out.printf("[GRP: %s]<%s>: %s\n", groupName, author.getName(), msg);
        }*/
        
        //If a bot issues a command do nothing, command response will only be seen in chat, not bot logs.
        if (bot == true)
        {
            
        }
        
        else if (msg.startsWith("https://open.spotify.com/track/")) //if link is Spotify, add it to the 5 length list
        {
            if (s == 5)
            {
                s = 0;
            }

            LinksL[s] = msg;
            s++;
            s1++;
            if (s1 > 5)
            {
                s1 = 5;
            }
        }
        
        else if (msg.startsWith("https://www.youtube.com/")) //if link is YouTube, add it to the 5 length list
        {
            if (y == 5)
            {
                y = 0;
            }

            LinksY[y] = msg;
            y++;
            y1++;
            if (y1 > 5)
            {
                y1 = 5;
            }
        }
        
        else if (msg.equals("!Commands") || msg.equals("!commands")) //List of commands (base user)
        {
            event.getChannel().sendMessage("All commands use '!'\nType:\n\nPlay  - followed by a youtube link (joins music voice channel and begins playback)\nQuit  - quits the voice channel and stops playback\nSkip  - skips to the next track in queue\n\nThe remaining commands are reserved for the operator.").queue();
        }
        
        else if (msg.startsWith("!OpenS")) //!Open Spotify command
        {          
            if (s1 == 0)
            {
                event.getChannel().sendMessage("There are no Spotify links to open.").queue();
            }
            
            else if (s1 == 5)
            {                
                try
                {
                    openSpotifyLink(LinksL, s1);
                    event.getChannel().sendMessage("Opening maximum 5 Spotify links.").queue();
                }
                catch (IOException ex)
                {
                    System.out.println("error: io");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (URISyntaxException ex)
                {
                    System.out.println("error: uri");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if (s1 == 1)
            {                
                try
                {
                    openSpotifyLink(LinksL, s1);
                    event.getChannel().sendMessage("Opening the latest Spotify link.").queue();
                }
                catch (IOException ex)
                {
                    System.out.println("error: io");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (URISyntaxException ex)
                {
                    System.out.println("error: uri");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
                try
                {
                    openSpotifyLink(LinksL, s1);
                    event.getChannel().sendMessage("Opening " + s1 + " Spotify links.").queue();
                }
                catch (IOException ex)
                {
                    System.out.println("error: io");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (URISyntaxException ex)
                {
                    System.out.println("error: uri");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
        else if (msg.startsWith("!OpenY")) //!Open YouTube command
        {
            if (y1 == 0)
            {
                event.getChannel().sendMessage("There are no YouTube links to open.").queue();
            }
            
            else if (y1 == 5)
            {
                try
                {
                    openSpotifyLink(LinksY, y1);
                    event.getChannel().sendMessage("Opening maximum 5 YouTube links.").queue();
                }
                catch (IOException ex)
                {
                    System.out.println("error: io");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (URISyntaxException ex)
                {
                    System.out.println("error: uri");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            else if (y1 == 1)
            {
                try
                {
                    openSpotifyLink(LinksY, y1);
                    event.getChannel().sendMessage("Opening latest Youtube Link.").queue();
                }
                catch (IOException ex)
                {
                    System.out.println("error: io");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (URISyntaxException ex)
                {
                    System.out.println("error: uri");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            else
            {
                try
                {
                    openSpotifyLink(LinksY, y1);
                    event.getChannel().sendMessage("Opening " + s1 + " YouTube links.").queue();
                }
                catch (IOException ex)
                {
                    System.out.println("error: io");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (URISyntaxException ex)
                {
                    System.out.println("error: uri");
                    Logger.getLogger(MusicBot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        else if (msg.equals ("!ListS"))
        {
            for (int z = 0; z < s1; z++)
            {
                event.getChannel().sendMessage((z+1) + ": " + LinksL[z]).queue();
            }
        }

        else if (msg.equals ("!ListY"))
        {
            for (int z = 0; z < y1; z++)
            {
                event.getChannel().sendMessage((z+1) + ": " + LinksY[z]).queue();
            }
        }
        
        else if (msg.startsWith ("!Play") || msg.startsWith ("!play") || msg.equals("!Quit") || msg.equals("!quit"))
        {
            Guild guild = event.getGuild();
            VoiceChannel vchannel = guild.getVoiceChannelsByName("music", true).get(0);
            AudioManager manager = guild.getAudioManager();
            
            if (msg.equals("!Quit") || msg.equals("!quit")) //Quits the voice channel
            {
                manager.closeAudioConnection();
                event.getChannel().sendMessage("Quitting voice channel.").queue();
            }
            else
            {
                String URL = msg.substring(6);

                manager.setSendingHandler(new AudioSendHandler() 
                {
                    @Override
                    public boolean canProvide() 
                    {
                        throw new UnsupportedOperationException("Not supported yet. [canProvide() error]");
                    }

                    @Override
                    public byte[] provide20MsAudio() 
                    {
                        throw new UnsupportedOperationException("Not supported yet. [provide20MsAudio() error]");
                    }
                });
            
                manager.openAudioConnection(vchannel);
            
            
                loadAndPlay(event.getTextChannel(), URL);
            }
        }
        
        else if (msg.equals("!Skip") || msg.equals("!skip")) //Skips the current track
        {
            TextChannel textChannel = event.getTextChannel();
            skipTrack(textChannel);
        }
    }
    

    public void openSpotifyLink(String[] LinksL, int n) throws IOException, URISyntaxException
    {
        Desktop d = Desktop.getDesktop();
        
        for (int x = 0; x < n; x++) //for all set links, up to user-input, open in chrome.
        {
            d.browse(new URI(LinksL[x]));
        }
    }

    public void openYouTubeLink(String[] LinksY, int n) throws IOException, URISyntaxException
    {
        Desktop d = Desktop.getDesktop();
        
        for (int x = 0; x < n; x++) //for all set links, up to user-input, open in chrome.
        {
            d.browse(new URI(LinksY[x]));
        }
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl) 
    {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() 
        {
            @Override
            public void trackLoaded(AudioTrack track) 
            {
                channel.sendMessage("Adding to queue: " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) 
            {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) 
                {
                    firstTrack = playlist.getTracks().get(0);
                }
            
            channel.sendMessage("Adding to queue: " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

            play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() //URL not found
            {
                channel.sendMessage("Nothing found by: " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) //File could not be loaded
            {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track)
    {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) //implement this
    {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) //used to connect to the music channel
    {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) 
        {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) 
            {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }   
        }
    }

}
