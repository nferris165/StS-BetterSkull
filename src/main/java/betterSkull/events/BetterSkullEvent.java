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
    private static final String POTION_MSG;
    private static final String CARD_MSG;
    private static final String GOLD_MSG;
    private static final String LEAVE_MSG;
    private int healAmt;
    private int potionCost;
    private int cardCost;
    private int goldCost;
    private int leaveCost;
    private static final int GOLD_REWARD = 90;
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
        } else {
            this.healAmt = (int)(AbstractDungeon.player.maxHealth * 0.1F);
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
        this.leaveCost = 6;
        this.cardCost = this.leaveCost;
        this.potionCost = this.leaveCost;
        this.goldCost = this.leaveCost;
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
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[4] + this.potionCost + OPTIONS[1]);
                        this.imageEventText.setDialogOption(OPTIONS[5] + 90 + OPTIONS[6] + this.goldCost + OPTIONS[1]);
                        this.imageEventText.setDialogOption(OPTIONS[3] + this.cardCost + OPTIONS[1]);
                        this.imageEventText.setDialogOption(OPTIONS[7] + this.leaveCost + OPTIONS[1]);
                        this.screen = CurScreen.ASK;
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
                        this.obtainReward(0);
                        return;
                    case 1:
                        this.obtainReward(1);
                        return;
                    case 2:
                        this.obtainReward(2);
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

    private void obtainReward(int slot) {
        switch(slot) {// 143
            case 0:
                AbstractDungeon.player.damage(new DamageInfo((AbstractCreature)null, this.potionCost, DamageInfo.DamageType.HP_LOSS));// 145
                this.damageTaken += this.potionCost;// 146
                ++this.potionCost;// 147
                this.optionsChosen = this.optionsChosen + "POTION ";// 148
                this.imageEventText.updateBodyText(POTION_MSG + ASK_AGAIN_MSG);// 149
                if (AbstractDungeon.player.hasRelic("Sozu")) {// 150
                    AbstractDungeon.player.getRelic("Sozu").flash();// 151
                } else {
                    AbstractPotion p = PotionHelper.getRandomPotion();// 153
                    this.potions.add(p.ID);// 154
                    AbstractDungeon.player.obtainPotion(p);// 155
                }
                break;
            case 1:
                AbstractDungeon.player.damage(new DamageInfo((AbstractCreature)null, this.goldCost, DamageInfo.DamageType.HP_LOSS));// 159
                this.damageTaken += this.goldCost;// 160
                ++this.goldCost;// 161
                this.optionsChosen = this.optionsChosen + "GOLD ";// 162
                this.imageEventText.updateBodyText(GOLD_MSG + ASK_AGAIN_MSG);// 163
                AbstractDungeon.effectList.add(new RainingGoldEffect(90));// 164
                AbstractDungeon.player.gainGold(90);// 165
                this.goldEarned += 90;// 166
                break;// 167
            case 2:
                AbstractDungeon.player.damage(new DamageInfo((AbstractCreature)null, this.cardCost, DamageInfo.DamageType.HP_LOSS));// 169
                this.damageTaken += this.cardCost;// 170
                ++this.cardCost;// 171
                this.optionsChosen = this.optionsChosen + "CARD ";// 172
                this.imageEventText.updateBodyText(CARD_MSG + ASK_AGAIN_MSG);// 173
                AbstractCard c = AbstractDungeon.returnColorlessCard(AbstractCard.CardRarity.UNCOMMON).makeCopy();// 174
                this.cards.add(c.cardID);// 175
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, (float)Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F));// 176
                break;// 178
            default:
                BetterSkull.logger.info("This should never happen.");
        }

        this.imageEventText.clearAllDialogs();// 183
        this.imageEventText.setDialogOption(OPTIONS[4] + this.potionCost + OPTIONS[1]);// 184
        this.imageEventText.setDialogOption(OPTIONS[5] + 90 + OPTIONS[6] + this.goldCost + OPTIONS[1]);// 185
        this.imageEventText.setDialogOption(OPTIONS[3] + this.cardCost + OPTIONS[1]);// 186
        this.imageEventText.setDialogOption(OPTIONS[7] + this.leaveCost + OPTIONS[1]);// 187
    }// 188

    private void setLeave() {
        this.imageEventText.updateBodyText(LEAVE_MSG);// 191
        this.imageEventText.clearAllDialogs();// 192
        this.imageEventText.setDialogOption(OPTIONS[8]);// 193
        this.screen = CurScreen.COMPLETE;// 194
    }

    static {
        INTRO_2_MSG = DESCRIPTIONS[1];
        ASK_AGAIN_MSG = DESCRIPTIONS[2];
        POTION_MSG = DESCRIPTIONS[4];
        CARD_MSG = DESCRIPTIONS[5];
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
