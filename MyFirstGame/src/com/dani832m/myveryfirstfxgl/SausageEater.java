//Angiver hvilken package spillets kildekode er placeret i
package com.dani832m.myveryfirstfxgl;

//Importerer nødvendige klasser fra libraries
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.CollidableComponent;
import com.almasb.fxgl.entity.control.KeepOnScreenControl;
import com.almasb.fxgl.entity.control.OffscreenCleanControl;
import com.almasb.fxgl.entity.view.ScrollingBackgroundView;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.scene.FXGLMenu;
import com.almasb.fxgl.scene.SceneFactory;
import com.almasb.fxgl.scene.menu.FXGLDefaultMenu;
import com.almasb.fxgl.scene.menu.MenuType;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

/**
 * THE SAUSAGE EATER - ET LILLE JAVA-SPIL UDVIKLET MED BIBLIOTEKET FXGL
 * Klassen "SausageEater" indeholder den samlede kildekode for spillet.
 * Klassen nedarver fra superklassen "GameApplication", som er en del af FXGL-biblioteket.
 *
 * @author Daniel Lyck <n4@n4.dk>
 * @version 1.0
 * @since 1.0
 */
public class SausageEater extends GameApplication {

    //Deklarerer variablen "player", som initialiseres i metoden "initGame"
    private Entity player;

    //Deklarerer variablen "lilSausage", som initialiseres i "onCollisionBegin"
    private Entity lilSausage; //Bruges ikke direkte på nuværende tidspunkt

    //En enum, der definerer hvilke enhedestyper, der findes i spillet
    public enum EntityType {
        PLAYER, SAUSAGE
    }

    /**
     * Metode, der definerer spilvinduets generelle specs.
     *
     * @param settings Tager parameter "settings" af typen "GameSettings".
     * @return void Returnerer ingenting.
     */
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(900); //Vinduets bredde
        settings.setHeight(660); //Vinduets højde
        settings.setTitle("The Sausage Eater"); //Titlen i vinduets top
        settings.setVersion("1.0"); //Spillets version (Står umiddelbart efter titlen)
        settings.setManualResizeEnabled(false); //Spilvinduets størrelse kan ikke ændres
        settings.setMenuEnabled(true); //Tilføjer default menu
        settings.setSceneFactory(new MySceneFactory()); //Sætter customized menu
        getAudioPlayer().loopBGM("bgm.mp3"); //Baggrundsmusik (Kører i loop)
    }

    /**
     * Klasse, der samlet set definerer customized menu.
     * Nedarver fra superklassen "SceneFactory", som er en del af FXGL-biblioteket.
     */
    public static class MySceneFactory extends SceneFactory {

        /**
         * Metode, der overordnet set håndterer baggrund og title i Main Menu screen.
         *
         * @param app Tager parameter "app" af typen "GameApplication" fra FXGL.
         * @return FXGLMenu Returnerer "FXGLMenu".
         */
        @NotNull
        @Override
        public FXGLMenu newMainMenu(@NotNull GameApplication app) {
            return new FXGLDefaultMenu(app, MenuType.MAIN_MENU) {
                @Override
                protected Node createBackground(double width, double height) {
                    return FXGL.getAssetLoader().loadTexture("inGame_bg.png");
                }

                //Overskriften i Main Menu-skærmen (Anvend ikke nogen!)
                @Override
                protected Node createTitleView(String title) {
                    return new Text("");
                }
            };
        }

        /**
         * Metode, der overordnet set håndterer baggrund og title i Game Menu screen.
         *
         * @param app Tager parameter "app" af typen "GameApplication" fra FXGL.
         * @return FXGLMenu Returnerer "FXGLMenu".
         */
        @NotNull
        @Override
        public FXGLMenu newGameMenu(@NotNull GameApplication app) {
            return new FXGLDefaultMenu(app, MenuType.GAME_MENU) {
                @Override
                    protected Node createBackground(double width, double height) {
                        return FXGL.getAssetLoader().loadTexture("inGame_bg.png");
                }

                //Overskriften i Game Menu-skærmen (Anvend ikke nogen!)
                @Override
                protected Node createTitleView(String title) {
                    return new Text("");
                }
            };
        }
    }

    /**
     * Definerer metoden "initGame", der indeholder specs for de elementer, der er i spillet ved opstart.
     *
     * @return void Returnerer ingenting.
     */
    @Override
    protected void initGame() {
        //Vores "player" konstrueres
        player = Entities.builder() //"builder" opretter enheden
                .type(EntityType.PLAYER) //Enhedstypen sættes via enum "EntityType"
                .at(100, 500) //Enhedens størrelse i pixels
                .viewFromTextureWithBBox("player.png") //Giver enheden sit udseende gennem .png-fil
                .with(new CollidableComponent(true)) //Angiver at enheden kan kollidere
                .with(new KeepOnScreenControl(true, true)) //Enheden kan ikke rykkes udenfor screen
                .buildAndAttach(getGameWorld()); //Frigiver enhed til spillet

        /* Endnu en enhed konstrueres, denne gang er det den første pølse-enhed. Samme princip som "player".
        Behøves dog ikke initialiseres til variabel, da vi ikke håndterer den på samme måde som "player" */
        Entities.builder()
                .type(EntityType.SAUSAGE)
                .at(500, 200) //Den første pølse vil altid ligge på denne placering
                .viewFromTextureWithBBox("lilSausage.png")
                .with(new CollidableComponent(true))
                .buildAndAttach(getGameWorld());

        //Nedenstående eksekveres, når der er spist præcis 50 pølser
        getGameState().<Integer>addListener("sausageCount", (prev, now) -> {
            if (now == 50) {
                getAudioPlayer().playSound("50sausages.wav"); //Afspiller lyd

                //Laver en KÆMPE pølse et random sted
                Entities.builder()
                        .type(EntityType.SAUSAGE)
                        //Bruger her FXGLMath klassens random-metode til at spawne pølsen et tilfældigt sted
                        .at(FXGLMath.random(getWidth() - 64), (FXGLMath.random(getHeight() - 64)))
                        .viewFromTextureWithBBox("bigSausage.png")
                        .with(new CollidableComponent(true))
                        .with(new OffscreenCleanControl())
                        .buildAndAttach(getGameWorld());
            }
        }); //Kodestykket for 50 pølser slutter her
    }

    /**
     * Metode, der er den egentlige "handler" af kollideringen mellem enheder.
     *
     * @return void Returnerer ingenting.
     */
    @Override
    protected void initPhysics() { //Ingen parametre
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.SAUSAGE) {

            /**
             * Metode der afvikles, hvis rækkefølgen af enhedstyper er den samme som i constructoren.
             *
             * @param player Playeren består af enhedstypen "PLAYER" (Initialisering i "initGame")
             * @param sausage Sausage består af enhedstypen "SAUSAGE"
             */
            @Override
            protected void onCollisionBegin(Entity player, Entity sausage) { //Returnerer ingenting
                sausage.removeFromWorld(); //Fjerner pølsen xD
                getAudioPlayer().playSound("haps.wav"); //Afspiller lyd ved kollision
                getGameState().increment("sausageCount", +1); //Inkrementerer count for antal pølser

                //Laver en ny lille pølse et random sted
                lilSausage = Entities.builder()
                            .type(EntityType.SAUSAGE)
                            //Bruger her FXGLMath klassens random-metode til at spawne pølsen et tilfældigt sted
                            .at(FXGLMath.random(getWidth() - 64), (FXGLMath.random(getHeight() - 64)))
                            .viewFromTextureWithBBox("lilSausage.png")
                            .with(new CollidableComponent(true))
                            .with(new OffscreenCleanControl())
                            .buildAndAttach(getGameWorld());
            }
        });
    }

    /**
     * Metode, der håndterer bruger-inputtet (Her er det styring af "player")
     */
    @Override
    protected void initInput() {
        Input input = getInput(); //Angiver, at variablen "input" håndterer metoden "getInput()"

        //Nedenstående tilføjer funktionalitet til variablen "input"
        input.addAction(new UserAction("Ryk til højre") {
            @Override
            protected void onAction() {
                player.translateX(5); //Ryk "player" 5 pixels til højre
                getGameState().increment("pixelsMoved", +5); //Inkrementer "pixelsMoved" med 5
            }
        }, KeyCode.D); //Denne handling sker, når der trykkes "D" på keyboardet

        //Samme fremgangsmåde, nu bare når "player" går til venstre
        input.addAction(new UserAction("Ryk til venstre") {
            @Override
            protected void onAction() {
                player.translateX(-5); //Minus fordi vi går til venstre på x-aksen
                getGameState().increment("pixelsMoved", +5);
            }
        }, KeyCode.A);

        //Samme fremgangsmåde, nu bare når "player" går op
        input.addAction(new UserAction("Ryk op") {
            @Override
            protected void onAction() {
                player.translateY(-5);
                getGameState().increment("pixelsMoved", +5);
            }
        }, KeyCode.W);

        //Samme fremgangsmåde, nu bare når "player" går ned
        input.addAction(new UserAction("Ryk ned") {
            @Override
            protected void onAction() {
                player.translateY(5);
                getGameState().increment("pixelsMoved", +5);
            }
        }, KeyCode.S);

        //Afspil bøvs og vis emoji, når der trykkes på tasten "B"
        input.addAction(new UserAction("Slå bøvs") {
            @Override
            protected void onActionBegin() {
                //Tilføjer emoji til pølsevognen
                Texture smelly = getAssetLoader().loadTexture("smellyBurp.png");
                smelly.setTranslateX(735);
                smelly.setTranslateY(504);

                getGameScene().addUINode(smelly); //Frigiver til scene graph
                //Afspiller bøvs
                getAudioPlayer().playSound("burp.mp3"); //Lokaliseret i "sounds"
            }
        }, KeyCode.B);
    }

    /**
     * Metode, der håndterer spilvariabler.
     *
     * @param vars Tager parameter "vars".
     */
    @Override
    protected void initGameVars(Map < String, Object > vars) {
        vars.put("pixelsMoved", 0); //Startværdi er 0
        vars.put("sausageCount", 0); //Startværdi er 0
    }

    /**
     * Følgende metode tilføjer elementer til selve bruger-interfacet.
     */
    @Override
    protected void initUI() {
        //Tilføjer og sætter baggrunden
        Texture background = getAssetLoader().loadTexture("background.png"); //Lokation i mappen "assets"
        ScrollingBackgroundView bg = new ScrollingBackgroundView(background, Orientation.HORIZONTAL);

        //Tilføjer og sætter "textHeading", som fungerer som overskrift
        Text textHeading = new Text("The Sausage Eater");
        textHeading.setFont(Font.font("Verdana", 40));
        textHeading.setTranslateX(25);
        textHeading.setTranslateY(60);
        textHeading.setFill(Color.GREEN);

        //Tilføjer og sætter den lille tekst "Pixels rykket"
        Text textPxMoved = new Text("Pixels rykket:");
        textPxMoved.setFont(Font.font("Verdana", 15));
        textPxMoved.setTranslateX(500);
        textPxMoved.setTranslateY(55);
        textPxMoved.setFill(Color.RED);

        //Tilføjer og sætter antallet af pixels, der er rykket (Håndteres i "initGameVars")
        Text pixels = new Text();
        pixels.setFont(Font.font("Verdana", 15));
        pixels.setTranslateX(615);
        pixels.setTranslateY(55);

        //Forbinder vores "pixels" med "pixelsMoved"
        pixels.textProperty().bind(getGameState().intProperty("pixelsMoved").asString());

        //Tilføjer og sætter teksten "Pølser spist"
        Text textSausages = new Text("Pølser spist:");
        textSausages.setFont(Font.font("Verdana", 15));
        textSausages.setTranslateX(695);
        textSausages.setTranslateY(55);
        textSausages.setFill(Color.RED);

        //Tilføjer og sætter antallet af spiste pølser
        Text sausages = new Text();
        sausages.setFont(Font.font("Verdana", 15));
        sausages.setTranslateX(800);
        sausages.setTranslateY(55);

        //Forbinder vores "sausages" med "sausageCount"
        sausages.textProperty().bind(getGameState().intProperty("sausageCount").asString());

        //Tilføjer og sætter "hotDogStand" med tilhørende .png-fil
        Texture hotDogStand = getAssetLoader().loadTexture("hotDogStand.png");
        hotDogStand.setTranslateX(700);
        hotDogStand.setTranslateY(470);

        //Nu tilføjer vi alle de definerede elementer til scene graph
        getGameScene().addGameView(bg); //Baggrunden
        getGameScene().addUINode(textHeading); //Overskriften
        getGameScene().addUINode(textPxMoved); //Teksten "Pixels rykket"
        getGameScene().addUINode(pixels); //Antallet af pixels
        getGameScene().addUINode(textSausages); //Teksten "Pølser spist"
        getGameScene().addUINode(sausages); //Antallet af spiste pølser
        getGameScene().addUINode(hotDogStand); //Pølsevognen
    }

    /**
     * Programeksekvering, der launcher vores spil (Som vi generelt kender det fra Java FX)
     *
     */
    public static void main(String[] args) {
        launch(args);
    }

} //Hele main klassen slutter