package betterSkull.patches;

import betterSkull.events.BetterSkullEvent;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.city.KnowingSkull;
import com.megacrit.cardcrawl.helpers.EventHelper;

public class EventHelperPatch {
    @SpirePatch(
            clz = EventHelper.class,
            method = "getEvent"
    )

    public static class EventSwapPatch {
        public static AbstractEvent Postfix(AbstractEvent __result, String key){

            if (__result instanceof KnowingSkull) {

                return new BetterSkullEvent();
            }
            return __result;
        }
    }
}