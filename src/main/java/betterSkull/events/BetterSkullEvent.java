package betterSkull.events;

import betterSkull.BetterSkull;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.max;

public class BetterSkullEvent extends AbstractImageEvent {

    public static final String ID = BetterSkull.makeID("BetterSkull");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);

    private static final String NAME = eventStrings.NAME;
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String IMG = "images/events/knowingSkull.jpg";

    private static final String REFUTE_MSG;
    private static final String INTRO_2_MSG;
    private static final String ASK_AGAIN_MSG;
    private static final String UPGRADE_MSG;
    private static final String RELIC_MSG;
    private static final String GOLD_MSG;
    private static final String LEAVE_MSG;
    private int healAmt;
    private int relicCost;
    private int upgradeCost;
    private int goldCost;
    private int leaveCost;
    private int goldReward;
    private CurScreen screen;
    private String optionsChosen;
    private int damageTaken;
    private int goldEarned;
    private List<String> potions;
    private List<String> cards;
    private List<Reward> options;

    public BetterSkullEvent() {
        super(NAME, DESCRIPTIONS[0], IMG);

        if (AbstractDungeon.ascensionLevel >= 15) {
            this.healAmt = (int)(AbstractDungeon.player.maxHealth * 0.05F);
            this.goldReward = 75;
        } else {
            this.healAmt = (int)(AbstractDungeon.player.maxHealth * 0.1F);
            this.goldReward = 100;
        }

        this.screen = CurScreen.INTRO_1;
        this.optionsChosen = "";
        this.options = new ArrayList<>();
        this.imageEventText.setDialogOption(OPTIONS[0]);
        this.imageEventText.setDialogOption(OPTIONS[9] + healAmt + OPTIONS[10]);
        this.options.add(Reward.CARD);
        this.options.add(Reward.GOLD);
        this.options.add(Reward.POTION);
        this.options.add(Reward.LEAVE);
        this.leaveCost = 1;
        this.upgradeCost = max((int)(AbstractDungeon.player.maxHealth * 0.1F), 6);
        this.relicCost = max((int)(AbstractDungeon.player.maxHealth * 0.2F), 6);
        this.goldCost = max((int)(AbstractDungeon.player.maxHealth * 0.1F), 6);
        this.damageTaken = 0;
        this.goldEarned = 0;
        this.cards = new ArrayList<>();
        this.potions = new ArrayList<>();
    }

    @Override
    public void onEnterRoom() {
        if (Settings.AMBIANCE_ON) {
            CardCrawlGame.sound.play("EVENT_SKULL");
        }

    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch(this.screen) {
            case INTRO_1:
                switch(buttonPressed) {
                    case 0:
                        this.imageEventText.updateBodyText(INTRO_2_MSG);
                        this.setChoices();
                        return;
                    case 1:
                        this.imageEventText.updateBodyText(REFUTE_MSG);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[8]);
                        AbstractDungeon.player.heal(healAmt);
                        this.screen = CurScreen.COMPLETE;
                        return;
                    default:
                        return;
                }
            case ASK:
                CardCrawlGame.sound.play("DEBUFF_2");
                switch(buttonPressed) {
                    case 0:
                        AbstractDungeon.player.damage(new DamageInfo(null, this.goldCost, DamageInfo.DamageType.HP_LOSS));
                        ++this.goldCost;
                        this.imageEventText.updateBodyText(GOLD_MSG + ASK_AGAIN_MSG);
                        AbstractDungeon.effectList.add(new RainingGoldEffect(goldReward));
                        AbstractDungeon.player.gainGold(goldReward);
                        this.setChoices();
                        //Metrics
                        this.damageTaken += this.goldCost;
                        this.optionsChosen = this.optionsChosen + "GOLD ";
                        this.goldEarned += goldReward;
                        return;
                    case 1:
                        AbstractDungeon.player.damage(new DamageInfo(null, this.upgradeCost, DamageInfo.DamageType.HP_LOSS));
                        ++this.upgradeCost;
                        this.imageEventText.updateBodyText(UPGRADE_MSG + ASK_AGAIN_MSG);
                        //TODO
                        this.setChoices();
                        //Metrics
                        this.damageTaken += this.upgradeCost;
                        this.optionsChosen = this.optionsChosen + "UPGRADE ";
                        return;
                    case 2:
                        AbstractDungeon.player.damage(new DamageInfo(null, this.relicCost, DamageInfo.DamageType.HP_LOSS));
                        ++this.relicCost;
                        this.imageEventText.updateBodyText(RELIC_MSG + ASK_AGAIN_MSG);
                        //TODO
                        this.setChoices();
                        //Metrics
                        this.damageTaken += this.relicCost;
                        this.optionsChosen = this.optionsChosen + "RELIC ";
                        return;
                    default:
                        AbstractDungeon.player.damage(new DamageInfo(null, this.leaveCost, DamageInfo.DamageType.HP_LOSS));
                        this.damageTaken += this.leaveCost;
                        this.setLeave();
                        return;
                }
            case COMPLETE:
                /*
                logMetric("Knowing Skull", this.optionsChosen, this.cards, (List)null, (List)null,
                        (List)null, (List)null, this.potions, (List)null, this.damageTaken, 0,
                        0, 0, this.goldEarned, 0);
                        */
                this.openMap();
        }

    }

    private void setChoices(){
        this.imageEventText.clearAllDialogs();
        this.imageEventText.setDialogOption(OPTIONS[5] + goldReward + OPTIONS[6] + this.goldCost + OPTIONS[1]);
        this.imageEventText.setDialogOption(OPTIONS[3] + this.upgradeCost + OPTIONS[1]);
        this.imageEventText.setDialogOption(OPTIONS[4] + this.relicCost + OPTIONS[1]);
        this.imageEventText.setDialogOption(OPTIONS[7] + this.leaveCost + OPTIONS[1]);
        this.screen = CurScreen.ASK;
    }

    private void setLeave() {
        this.imageEventText.updateBodyText(LEAVE_MSG);
        this.imageEventText.clearAllDialogs();
        this.imageEventText.setDialogOption(OPTIONS[8]);
        this.screen = CurScreen.COMPLETE;
    }

    static {
        INTRO_2_MSG = DESCRIPTIONS[1];
        ASK_AGAIN_MSG = DESCRIPTIONS[2];
        UPGRADE_MSG = DESCRIPTIONS[4];
        RELIC_MSG = DESCRIPTIONS[5];
        GOLD_MSG = DESCRIPTIONS[6];
        LEAVE_MSG = DESCRIPTIONS[7];
        REFUTE_MSG = DESCRIPTIONS[8];
    }

    private enum Reward {
        POTION,
        LEAVE,
        GOLD,
        CARD;

        Reward() {
        }
    }

    private enum CurScreen {
        INTRO_1,
        ASK,
        COMPLETE;

        CurScreen() {
        }
    }
}
