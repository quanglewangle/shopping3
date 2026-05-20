package com.quanglewangle.peter.shopping3;

/**
 * Created by peter on 17/08/2017.
 */

public class NameValuePair<KEY, VALUE> {


        private final KEY left;
        private final VALUE right;

        public NameValuePair(KEY left, VALUE right) {
            this.left = left;
            this.right = right;
        }

        public KEY getLeft() { return left; }
        public VALUE getRight() { return right; }

}

